package com.smartsplit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.smartsplit.dao.CategoryDAO;
import com.smartsplit.database.DatabaseInitializer;
import com.smartsplit.model.Balance;
import com.smartsplit.model.Expense;
import com.smartsplit.model.Group;
import com.smartsplit.model.Member;
import com.smartsplit.model.Settlement;
import com.smartsplit.model.User;
import com.smartsplit.service.AuthService;
import com.smartsplit.service.BalanceService;
import com.smartsplit.service.ExpenseService;
import com.smartsplit.service.GroupService;
import com.smartsplit.service.ReportService;
import com.smartsplit.service.SettlementService;
import com.smartsplit.util.FormatUtil;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    private final AuthService auth = new AuthService();
    private final GroupService groupService = new GroupService();
    private final ExpenseService expenseService = new ExpenseService();
    private final BalanceService balanceService = new BalanceService();
    private final SettlementService settlementService = new SettlementService();
    private final ReportService reportService = new ReportService();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private Stage stage;
    private BorderPane root;
    private User current;
    private Group currentGroup;
    private final Label pageTitle = new Label("Dashboard");
    private Button navDashboardButton;
    private Button navGroupsButton;
    private Button navCreateButton;
    private Button navDemoButton;
    private final List<Button> sidebarNavigation = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("SmartSplit v3 — Group Expense Management");
        stage.setResizable(true);

        // Fit the initial window to the usable desktop area. JavaFX dimensions are
        // logical pixels, which can be smaller than the physical screenshot size
        // on Windows systems using 125%/150% display scaling. A fixed 1360 x 850
        // scene could therefore extend beyond the edges of smaller usable desktops.
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double usableWidth = visualBounds.getWidth();
        double usableHeight = visualBounds.getHeight();
        double initialWidth = Math.min(1360, usableWidth * 0.97);
        double initialHeight = Math.min(850, usableHeight * 0.94);
        double minimumWidth = Math.min(1024, usableWidth * 0.90);
        double minimumHeight = Math.min(650, usableHeight * 0.88);

        stage.setMinWidth(minimumWidth);
        stage.setMinHeight(minimumHeight);
        stage.setWidth(Math.max(minimumWidth, initialWidth));
        stage.setHeight(Math.max(minimumHeight, initialHeight));
        stage.setX(visualBounds.getMinX() + (usableWidth - stage.getWidth()) / 2.0);
        stage.setY(visualBounds.getMinY() + (usableHeight - stage.getHeight()) / 2.0);
        stage.setOnCloseRequest(e -> {
            if (current == null) {
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
            confirm.setTitle("Close SmartSplit");
            confirm.setHeaderText("Leave your workspace?");
            confirm.setContentText("Your expenses and settlements are saved locally in SQLite. Closing the window will not delete them.");
            styleDialogPane(confirm);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                e.consume();
            } else {
                auth.logout();
            }
        });
        DatabaseInitializer.initialize();
        showLogin();
    }

    private Button button(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-button");
        return b;
    }

    private VBox card(Node... nodes) {
        VBox v = new VBox(10, nodes);
        v.getStyleClass().add("card");
        return v;
    }

    private void scene(Parent content) {
        // Keep the Scene at the Stage's actual size instead of forcing fixed 1024x650
        // minimums. The latter can recreate edge clipping on scaled/small displays.
        double sceneWidth = Math.max(1, stage.getWidth());
        double sceneHeight = Math.max(1, stage.getHeight());
        Scene scene = new Scene(content, sceneWidth, sceneHeight);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private Parent authShell(VBox form, String headline, String description) {
        HBox shell = new HBox();
        shell.getStyleClass().add("auth-shell");

        VBox hero = new VBox(22);
        hero.getStyleClass().add("auth-hero");
        hero.setPrefWidth(445);
        hero.setMinWidth(350);

        HBox logo = new HBox(11);
        logo.setAlignment(Pos.CENTER_LEFT);
        Label mark = new Label("S");
        mark.getStyleClass().add("brand-mark");
        VBox wordmark = new VBox(2);
        Label name = new Label("SmartSplit");
        name.getStyleClass().add("auth-hero-brand");
        Label caption = new Label("GROUP EXPENSES, MADE CLEAR");
        caption.getStyleClass().add("brand-caption");
        wordmark.getChildren().addAll(name, caption);
        logo.getChildren().addAll(mark, wordmark);

        Label eyebrow = new Label("A SMARTER WAY TO SHARE");
        eyebrow.getStyleClass().add("auth-hero-eyebrow");
        Label heroTitle = new Label(headline);
        heroTitle.getStyleClass().add("auth-hero-title");
        heroTitle.setWrapText(true);
        heroTitle.setMaxWidth(Double.MAX_VALUE);
        Label heroCopy = new Label(description);
        heroCopy.getStyleClass().add("auth-hero-copy");
        heroCopy.setWrapText(true);

        VBox features = new VBox(17,
                authFeature("↗", "Clear balances", "Know who owes and who should receive."),
                authFeature("◎", "Flexible splitting", "Equal, exact and percentage allocations."),
                authFeature("✓", "Local and private", "Your project data stays in your SQLite database."));
        Label footer = new Label("JAVA DESKTOP APPLICATION  •  SQLITE STORAGE");
        footer.getStyleClass().add("brand-caption");
        Label authorCredit = new Label("Made by Yuvraj Singh Sidhu, 24BCS10816");
        authorCredit.getStyleClass().add("author-credit");
        VBox footerBlock = new VBox(5, footer, authorCredit);
        footerBlock.setAlignment(Pos.CENTER_LEFT);
        Region spacer = spacer();
        hero.getChildren().addAll(logo, spacer, eyebrow, heroTitle, heroCopy, features, spacer(), footerBlock);

        StackPane formRegion = new StackPane(form);
        formRegion.getStyleClass().add("auth-form-region");
        HBox.setHgrow(formRegion, Priority.ALWAYS);
        shell.getChildren().addAll(hero, formRegion);
        return shell;
    }

    private VBox authFeature(String symbol, String title, String copy) {
        Label mark = new Label(symbol);
        mark.getStyleClass().add("auth-feature-mark");
        Label heading = new Label(title);
        heading.getStyleClass().add("auth-feature-title");
        Label subtitle = new Label(copy);
        subtitle.getStyleClass().add("auth-feature-copy");
        VBox text = new VBox(3, heading, subtitle);
        HBox row = new HBox(12, mark, text);
        row.getStyleClass().add("auth-feature");
        return new VBox(row);
    }

    private Region spacer() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }

    private void styleDialogPane(Dialog<?> dialog) {
        if (stage != null && dialog.getOwner() == null) {
            dialog.initOwner(stage);
        }
        String stylesheet = getClass().getResource("/css/style.css").toExternalForm();
        if (!dialog.getDialogPane().getStylesheets().contains(stylesheet)) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
        }
        if (!dialog.getDialogPane().getStyleClass().contains("app-dialog")) {
            dialog.getDialogPane().getStyleClass().add("app-dialog");
        }
    }

    private VBox sectionIntro(String title, String subtitle) {
        Label heading = new Label(title);
        heading.getStyleClass().add("section-title");
        Label copy = new Label(subtitle);
        copy.getStyleClass().add("section-subtitle");
        copy.setWrapText(true);
        return new VBox(5, heading, copy);
    }

    private void setActiveNavigation(Button active) {
        for (Button b : sidebarNavigation) {
            b.getStyleClass().remove("active");
        }
        if (active != null && !active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }

    private void setActiveFromTitle(String title) {
        if (title.equalsIgnoreCase("Dashboard")) {
            setActiveNavigation(navDashboardButton); 
        }else if (title.startsWith("My Groups") || title.contains("—") || title.equalsIgnoreCase("Groups")) {
            setActiveNavigation(navGroupsButton);
        }
    }

    private void emptyState(String title, String copy, VBox parent) {
        Label heading = new Label(title);
        heading.getStyleClass().add("empty-state-title");
        Label subtitle = new Label(copy);
        subtitle.getStyleClass().add("empty-state-copy");
        subtitle.setWrapText(true);
        VBox state = new VBox(heading, subtitle);
        state.getStyleClass().add("empty-state");
        parent.getChildren().add(state);
    }

    private void showLogin() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(450);
        form.getStyleClass().add("auth-card");

        Label eyebrow = new Label("WELCOME BACK");
        eyebrow.getStyleClass().add("auth-form-eyebrow");
        Label title = new Label("Sign in to SmartSplit");
        title.getStyleClass().add("auth-form-title");
        Label subtitle = new Label("Pick up where your group left off.");
        subtitle.getStyleClass().add("auth-form-copy");

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setMaxWidth(Double.MAX_VALUE);
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setMaxWidth(Double.MAX_VALUE);

        Button login = new Button("Sign In  →");
        login.getStyleClass().add("primary-button");
        login.setMaxWidth(Double.MAX_VALUE);
        Button demoLogin = new Button("Use Demo Account");
        demoLogin.getStyleClass().add("secondary-button");
        demoLogin.setMaxWidth(Double.MAX_VALUE);
        Button register = new Button("Create a new account");
        register.getStyleClass().add("ghost-button");
        register.setMaxWidth(Double.MAX_VALUE);
        Label hint = new Label("Demo credentials:  demo  /  demo123");
        hint.getStyleClass().add("hint");
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setAlignment(Pos.CENTER);

        login.setOnAction(e -> {
            try {
                current = auth.login(username.getText(), password.getText());
                showApp();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        password.setOnAction(e -> login.fire());
        demoLogin.setOnAction(e -> {
            username.setText("demo");
            password.setText("demo123");
            login.fire();
        });
        register.setOnAction(e -> showRegister());

        form.getChildren().addAll(eyebrow, title, subtitle, username, password, login, demoLogin, register, hint);
        scene(authShell(form,
                "Shared costs.\nClear minds.",
                "Organize group spending, understand every balance, and settle up without messy spreadsheets or repeated calculations."));
    }

    private void showRegister() {
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(470);
        form.getStyleClass().add("auth-card");

        Label eyebrow = new Label("GET STARTED");
        eyebrow.getStyleClass().add("auth-form-eyebrow");
        Label title = new Label("Create your account");
        title.getStyleClass().add("auth-form-title");
        Label subtitle = new Label("Your account manages the groups you create. Other participants can be added locally without extra logins.");
        subtitle.getStyleClass().add("auth-form-copy");
        subtitle.setWrapText(true);

        TextField name = new TextField();
        name.setPromptText("Full name");
        TextField username = new TextField();
        username.setPromptText("Choose a username");
        TextField email = new TextField();
        email.setPromptText("Email address");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password (at least 8 characters)");
        Button create = new Button("Create Account  →");
        create.getStyleClass().add("primary-button");
        create.setMaxWidth(Double.MAX_VALUE);
        Button back = new Button("Back to sign in");
        back.getStyleClass().add("ghost-button");
        back.setMaxWidth(Double.MAX_VALUE);

        create.setOnAction(e -> {
            try {
                auth.register(name.getText(), username.getText(), email.getText(), pass.getText());
                info("Account created successfully. You can now sign in.");
                showLogin();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        pass.setOnAction(e -> create.fire());
        back.setOnAction(e -> showLogin());
        form.getChildren().addAll(eyebrow, title, subtitle, name, username, email, pass, create, back);
        scene(authShell(form,
                "Make group\nspending simple.",
                "Bring your shared expenses into one local workspace. SmartSplit keeps the calculation logic transparent and your records on your computer."));
    }

    private void showApp() {
        root = new BorderPane();
        root.getStyleClass().add("app");

        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(21, 15, 17, 15));

        HBox brandRow = new HBox(11);
        brandRow.getStyleClass().add("brand-row");
        Label mark = new Label("S");
        mark.getStyleClass().add("brand-mark");
        VBox brandText = new VBox(2);
        Label brand = new Label("SmartSplit");
        brand.getStyleClass().add("brand");
        Label caption = new Label("GROUP FINANCE");
        caption.getStyleClass().add("brand-caption");
        brandText.getChildren().addAll(brand, caption);
        brandRow.getChildren().addAll(mark, brandText);

        VBox userCard = new VBox(5);
        userCard.getStyleClass().add("sidebar-user-card");
        Label userCaption = new Label("SIGNED IN AS");
        userCaption.getStyleClass().add("brand-caption");
        Label user = new Label(current.getName() + "\n@" + current.getUsername());
        user.getStyleClass().add("sidebar-user");
        user.setWrapText(true);
        userCard.getChildren().addAll(userCaption, user);

        Label mainLabel = new Label("WORKSPACE");
        mainLabel.getStyleClass().add("sidebar-section-label");
        navDashboardButton = button("⌂   Dashboard");
        navGroupsButton = button("▣   My Groups");
        navCreateButton = button("＋   Create Group");
        navDemoButton = button("✦   Demo Workspace");
        sidebarNavigation.clear();
        sidebarNavigation.addAll(List.of(navDashboardButton, navGroupsButton, navCreateButton, navDemoButton));

        navDashboardButton.setOnAction(e -> dashboard());
        navGroupsButton.setOnAction(e -> groupsView());
        navCreateButton.setOnAction(e -> createGroupDialog());
        navDemoButton.setOnAction(e -> loadDemoData());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label footerTitle = new Label("LOCAL-FIRST BY DESIGN");
        footerTitle.getStyleClass().add("sidebar-footer-title");
        Label footerCopy = new Label("Java 25  •  JavaFX  •  SQLite");
        footerCopy.getStyleClass().add("sidebar-footer-copy");
        Label authorCredit = new Label("Made by Yuvraj Singh Sidhu, 24BCS10816");
        authorCredit.getStyleClass().add("sidebar-footer-credit");
        authorCredit.setWrapText(true);
        authorCredit.setMaxWidth(Double.MAX_VALUE);
        VBox footer = new VBox(4, footerTitle, footerCopy, authorCredit);
        footer.getStyleClass().add("sidebar-footer");
        Button logout = button("↪   Sign Out");
        logout.setOnAction(e -> signOut());

        sidebar.getChildren().addAll(brandRow, userCard, new Separator(), mainLabel,
                navDashboardButton, navGroupsButton, navCreateButton, navDemoButton, spacer, footer, logout);
        root.setLeft(sidebar);

        HBox top = new HBox(14);
        top.getStyleClass().add("topbar");
        top.setAlignment(Pos.CENTER_LEFT);
        if (!pageTitle.getStyleClass().contains("page-title")) {
            pageTitle.getStyleClass().add("page-title");
        }
        VBox titleBox = new VBox(3, pageTitle);
        Label topCaption = new Label("GROUP EXPENSE MANAGEMENT");
        topCaption.getStyleClass().add("topbar-meta");
        titleBox.getChildren().add(topCaption);
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        HBox status = new HBox(7, new Region(), new Label("Local workspace"));
        status.getStyleClass().add("status-pill");
        status.getChildren().get(0).getStyleClass().add("status-dot");
        status.getChildren().get(1).getStyleClass().add("status-label");
        Button topSignOut = new Button("Sign Out");
        topSignOut.getStyleClass().add("ghost-button");
        topSignOut.setOnAction(e -> signOut());
        top.getChildren().addAll(titleBox, topSpacer, status, topSignOut);
        root.setTop(top);

        dashboard();
        scene(root);
    }

    private void signOut() {
        auth.logout();
        current = null;
        currentGroup = null;
        showLogin();
    }

    private void setContent(Node n, String title) {
        pageTitle.setText(title);
        setActiveFromTitle(title);
        if (n instanceof VBox box && !box.getStyleClass().contains("page-content")) {
            box.getStyleClass().add("page-content");
        }
        ScrollPane scroll = new ScrollPane(n);
        scroll.setFitToWidth(true);
        scroll.setPannable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("content-scroll");
        root.setCenter(scroll);
    }

    private void dashboard() {
        VBox content = new VBox(21);
        content.setPadding(new Insets(28, 30, 34, 30));

        List<Group> groups = groupService.list(current.getId());
        BigDecimal owed = BigDecimal.ZERO;
        BigDecimal receive = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        int expenseCount = 0;
        List<Expense> recentExpenses = new ArrayList<>();
        Map<Integer, String> groupNames = new LinkedHashMap<>();

        for (Group group : groups) {
            groupNames.put(group.getId(), group.getName());
            for (Balance balance : balanceService.balances(group.getId())) {
                if (balance.member().getId() == current.getId()) {
                    if (balance.amount().signum() < 0) {
                        owed = owed.add(balance.amount().abs()); 
                    }else {
                        receive = receive.add(balance.amount());
                    }
                }
            }
            total = total.add(expenseService.totalSpent(group.getId()));
            List<Expense> groupExpenses = expenseService.list(group.getId());
            expenseCount += groupExpenses.size();
            recentExpenses.addAll(groupExpenses);
        }
        recentExpenses.sort(Comparator.comparing(Expense::getExpenseDate).reversed());

        VBox welcome = new VBox(8);
        welcome.getStyleClass().add("hero-banner");
        Label eyebrow = new Label("YOUR WORKSPACE");
        eyebrow.getStyleClass().add("auth-hero-eyebrow");
        Label heroTitle = new Label("Good to see you, " + current.getName().split(" ")[0] + ".");
        heroTitle.getStyleClass().add("hero-title");
        Label heroCopy = new Label("Keep shared spending organized, make balances easy to understand, and settle up with confidence.");
        heroCopy.getStyleClass().add("hero-copy");
        heroCopy.setWrapText(true);
        HBox bannerActions = new HBox(10);
        Button quickCreate = new Button("＋  Create Group");
        quickCreate.getStyleClass().add("primary-button");
        Button quickDemo = new Button("Open Demo Workspace");
        quickDemo.getStyleClass().add("secondary-button");
        quickCreate.setOnAction(e -> createGroupDialog());
        quickDemo.setOnAction(e -> loadDemoData());
        bannerActions.getChildren().addAll(quickCreate, quickDemo);
        welcome.getChildren().addAll(eyebrow, heroTitle, heroCopy, bannerActions);

        HBox stats = new HBox(14,
                stat("Groups", String.valueOf(groups.size())),
                stat("Total group spending", FormatUtil.shortMoney(total)),
                stat("You owe", FormatUtil.shortMoney(owed)),
                stat("Owed to you", FormatUtil.shortMoney(receive)),
                stat("Expenses", String.valueOf(expenseCount)));
        stats.setFillHeight(true);

        HBox activityHead = new HBox(10);
        activityHead.setAlignment(Pos.CENTER_LEFT);
        VBox activityTitle = sectionIntro("Recent activity", "The latest expense records across groups you belong to.");
        Region activitySpacer = new Region();
        HBox.setHgrow(activitySpacer, Priority.ALWAYS);
        Button viewGroups = new Button("View all groups");
        viewGroups.getStyleClass().add("ghost-button");
        viewGroups.setOnAction(e -> groupsView());
        activityHead.getChildren().addAll(activityTitle, activitySpacer, viewGroups);

        VBox activity = new VBox(9);
        activity.getStyleClass().add("activity-list");
        if (recentExpenses.isEmpty()) {
            emptyState("Nothing to show yet", "Create a group and add an expense. Your recent activity will appear here.", activity);
        } else {
            int count = Math.min(5, recentExpenses.size());
            for (int i = 0; i < count; i++) {
                Expense expense = recentExpenses.get(i);
                HBox row = new HBox(13);
                row.getStyleClass().add("activity-row");
                row.setAlignment(Pos.CENTER_LEFT);
                Label mark = new Label("₹");
                mark.getStyleClass().add("activity-mark");
                VBox detail = new VBox(4);
                Label description = new Label(expense.getDescription());
                description.getStyleClass().add("row-title");
                String groupName = groupNames.getOrDefault(expense.getGroupId(), "Group");
                Label subtitle = new Label(groupName + "  •  " + expense.getExpenseDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
                subtitle.getStyleClass().add("row-copy");
                detail.getChildren().addAll(description, subtitle);
                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);
                Label amount = new Label(FormatUtil.shortMoney(expense.getAmount()));
                amount.getStyleClass().add("amount-text");
                row.getChildren().addAll(mark, detail, rowSpacer, amount);
                activity.getChildren().add(row);
            }
        }

        HBox lower = new HBox(15);
        VBox groupCard = card(sectionIntro("Your groups", "Open a group to manage members, expenses and settlements."));
        if (groups.isEmpty()) {
            emptyState("No groups created", "Create your first expense group to get started.", groupCard);
        } else {
            for (Group group : groups.stream().limit(3).toList()) {
                HBox groupRow = new HBox(10);
                groupRow.setAlignment(Pos.CENTER_LEFT);
                Label icon = new Label("↗");
                icon.getStyleClass().add("group-avatar");
                VBox details = new VBox(3);
                Label gName = new Label(group.getName());
                gName.getStyleClass().add("row-title");
                Label gMeta = new Label(groupService.members(group.getId()).size() + " members  •  " + FormatUtil.shortMoney(expenseService.totalSpent(group.getId())));
                gMeta.getStyleClass().add("row-copy");
                details.getChildren().addAll(gName, gMeta);
                Region push = new Region();
                HBox.setHgrow(push, Priority.ALWAYS);
                Button open = new Button("Open");
                open.getStyleClass().add("secondary-button");
                open.setOnAction(e -> {
                    currentGroup = group;
                    groupDetails();
                });
                groupRow.getChildren().addAll(icon, details, push, open);
                groupCard.getChildren().add(groupRow);
            }
        }
        VBox.setVgrow(groupCard, Priority.ALWAYS);
        VBox shortcuts = card(sectionIntro("Quick actions", "Common tasks, one click away."));
        Button newExpense = new Button("＋  Add Expense");
        newExpense.getStyleClass().add("primary-button");
        newExpense.setMaxWidth(Double.MAX_VALUE);
        newExpense.setOnAction(e -> {
            if (groups.isEmpty()) {
                createGroupDialog(); 
            }else {
                currentGroup = groups.get(0);
                addExpenseDialog();
            }
        });
        Button demoData = new Button("✦  Load demonstration data");
        demoData.getStyleClass().add("secondary-button");
        demoData.setMaxWidth(Double.MAX_VALUE);
        demoData.setOnAction(e -> loadDemoData());
        Button reports = new Button("▥  View group reports");
        reports.getStyleClass().add("secondary-button");
        reports.setMaxWidth(Double.MAX_VALUE);
        reports.setOnAction(e -> {
            if (groups.isEmpty()) {
                info("Create a group before opening reports.");
            } else {
                currentGroup = groups.get(0);
                reportsView();
            }
        });
        shortcuts.getChildren().addAll(newExpense, demoData, reports);
        lower.getChildren().addAll(groupCard, shortcuts);
        HBox.setHgrow(groupCard, Priority.ALWAYS);
        HBox.setHgrow(shortcuts, Priority.ALWAYS);
        shortcuts.setPrefWidth(280);
        groupCard.setPrefWidth(600);

        content.getChildren().addAll(welcome, stats, activityHead, activity, lower);
        setContent(content, "Dashboard");
    }

    private VBox stat(String name, String value) {
        Label label = new Label(name);
        label.getStyleClass().add("stat-label");
        Label metric = new Label(value);
        metric.getStyleClass().add("stat-value");
        VBox box = new VBox(8, label, metric);
        box.getStyleClass().add("stat-card");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void groupsView() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(28, 30, 34, 30));
        HBox heading = new HBox(12);
        heading.setAlignment(Pos.CENTER_LEFT);
        VBox intro = sectionIntro("Your groups", "Every trip, shared home, or group plan in one organized place.");
        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        Button add = new Button("＋  Create Group");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> createGroupDialog());
        heading.getChildren().addAll(intro, push, add);

        VBox list = new VBox(12);
        List<Group> groups = groupService.list(current.getId());
        for (Group group : groups) {
            BigDecimal total = expenseService.totalSpent(group.getId());
            int memberCount = groupService.members(group.getId()).size();
            int expenses = expenseService.list(group.getId()).size();

            HBox row = new HBox(15);
            row.getStyleClass().add("group-card");
            row.setAlignment(Pos.CENTER_LEFT);
            Label icon = new Label("↗");
            icon.getStyleClass().add("group-avatar");
            VBox detail = new VBox(5);
            Label title = new Label(group.getName());
            title.getStyleClass().add("row-title");
            Label description = new Label(group.getDescription() == null || group.getDescription().isBlank()
                    ? "Shared expense group" : group.getDescription());
            description.getStyleClass().add("row-copy");
            description.setWrapText(true);
            detail.getChildren().addAll(title, description);
            HBox.setHgrow(detail, Priority.ALWAYS);

            VBox metricOne = new VBox(4, new Label("MEMBERS"), new Label(String.valueOf(memberCount)));
            metricOne.getStyleClass().add("group-metric");
            VBox metricTwo = new VBox(4, new Label("EXPENSES"), new Label(String.valueOf(expenses)));
            metricTwo.getStyleClass().add("group-metric");
            VBox metricThree = new VBox(4, new Label("TOTAL SPENT"), new Label(FormatUtil.shortMoney(total)));
            metricThree.getStyleClass().add("group-metric");
            Button open = new Button("Open group  →");
            open.getStyleClass().add("primary-button");
            open.setOnAction(e -> {
                currentGroup = group;
                groupDetails();
            });
            row.getChildren().addAll(icon, detail, metricOne, metricTwo, metricThree, open);
            list.getChildren().add(row);
        }
        if (groups.isEmpty()) {
            emptyState("No groups yet", "Create your first group. You can add local participants without asking them to register accounts.", list);
        }
        content.getChildren().addAll(heading, list);
        setContent(content, "My Groups");
    }

    private void createGroupDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Group");
        styleDialogPane(dialog);
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(13);
        form.setPadding(new Insets(18));
        TextField name = new TextField();
        name.setPromptText("e.g. Goa Trip 2026");
        TextField description = new TextField();
        description.setPromptText("A short description (optional)");
        form.addRow(0, new Label("Group name"), name);
        form.addRow(1, new Label("Description"), description);
        GridPane.setHgrow(name, Priority.ALWAYS);
        GridPane.setHgrow(description, Priority.ALWAYS);
        form.setMinWidth(430);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("primary-button");
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    int groupId = groupService.create(name.getText(), description.getText(), current.getId());
                    groupService.addMember(groupId, current.getId());
                    info("Group created successfully. You were added as the first member.");
                    groupsView();
                } catch (Exception ex) {
                    error(ex.getMessage());
                }
            }
        });
    }

    private void groupDetails() {
        VBox content = new VBox(21);
        content.setPadding(new Insets(28, 30, 34, 30));
        List<Member> members = groupService.members(currentGroup.getId());
        List<Expense> expenses = expenseService.list(currentGroup.getId());
        BigDecimal total = expenseService.totalSpent(currentGroup.getId());

        VBox banner = new VBox(7);
        banner.getStyleClass().add("hero-banner");
        Label eyebrow = new Label("GROUP WORKSPACE");
        eyebrow.getStyleClass().add("auth-hero-eyebrow");
        Label name = new Label(currentGroup.getName());
        name.getStyleClass().add("hero-title");
        Label description = new Label(currentGroup.getDescription() == null || currentGroup.getDescription().isBlank()
                ? "Manage shared spending for this group." : currentGroup.getDescription());
        description.getStyleClass().add("hero-copy");
        description.setWrapText(true);
        banner.getChildren().addAll(eyebrow, name, description);

        HBox stats = new HBox(14,
                stat("Participants", String.valueOf(members.size())),
                stat("Expenses", String.valueOf(expenses.size())),
                stat("Total spent", FormatUtil.shortMoney(total)));

        Label navHeading = new Label("MANAGE THIS GROUP");
        navHeading.getStyleClass().add("page-eyebrow");
        HBox nav = new HBox(9);
        Button membersBtn = new Button("♙  Members");
        membersBtn.getStyleClass().add("secondary-button");
        Button expensesBtn = new Button("↗  Expenses");
        expensesBtn.getStyleClass().add("secondary-button");
        Button balancesBtn = new Button("◈  Balances");
        balancesBtn.getStyleClass().add("secondary-button");
        Button settlementsBtn = new Button("⇄  Settlements");
        settlementsBtn.getStyleClass().add("secondary-button");
        Button reportsBtn = new Button("▥  Reports");
        reportsBtn.getStyleClass().add("secondary-button");
        Button back = new Button("← Groups");
        back.getStyleClass().add("ghost-button");
        Button delete = new Button(currentGroup.getCreatedBy() == current.getId() ? "Delete group" : "Delete group (owner only)");
        delete.getStyleClass().add("danger-button");
        boolean isGroupOwner = currentGroup.getCreatedBy() == current.getId();
        delete.setDisable(!isGroupOwner);
        if (!isGroupOwner) {
            delete.setTooltip(new Tooltip("Only the account that created this group can delete it."));
        }
        membersBtn.setOnAction(e -> membersView());
        expensesBtn.setOnAction(e -> expensesView());
        balancesBtn.setOnAction(e -> balancesView());
        settlementsBtn.setOnAction(e -> settlementsView());
        reportsBtn.setOnAction(e -> reportsView());
        delete.setOnAction(e -> deleteCurrentGroup());
        back.setOnAction(e -> groupsView());
        nav.getChildren().addAll(membersBtn, expensesBtn, balancesBtn, settlementsBtn, reportsBtn);
        HBox utilityActions = new HBox(9, back, delete);

        VBox overview = card(sectionIntro("At a glance", "Use the areas above to maintain participants, record expenses, inspect balances, and review the settlement history."));
        HBox latest = new HBox(10);
        Label latestMark = new Label("✓");
        latestMark.getStyleClass().add("activity-mark");
        VBox latestText = new VBox(3);
        Label latestTitle = new Label(expenses.isEmpty() ? "Ready for your first expense" : "Expense records are up to date");
        latestTitle.getStyleClass().add("row-title");
        Label latestCopy = new Label(expenses.isEmpty() ? "Add a transaction to see the group's financial position." : expenses.size() + " expense record(s) are stored in SQLite.");
        latestCopy.getStyleClass().add("row-copy");
        latestText.getChildren().addAll(latestTitle, latestCopy);
        latest.getChildren().addAll(latestMark, latestText);
        overview.getChildren().add(latest);

        content.getChildren().addAll(banner, stats, navHeading, nav, utilityActions, overview);
        setContent(content, currentGroup.getName());
        setActiveNavigation(navGroupsButton);
    }

    private void membersView() {
        VBox content = new VBox(17);
        content.setPadding(new Insets(28, 30, 34, 30));
        List<Member> members = groupService.members(currentGroup.getId());

        HBox existingRow = new HBox(10);
        existingRow.setAlignment(Pos.CENTER_LEFT);
        TextField username = new TextField();
        username.setPromptText("Username of an existing account");
        HBox.setHgrow(username, Priority.ALWAYS);
        Button addExisting = new Button("Add Existing");
        addExisting.getStyleClass().add("primary-button");
        addExisting.setOnAction(e -> {
            try {
                groupService.addMemberByUsername(currentGroup.getId(), username.getText());
                username.clear();
                membersView();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        existingRow.getChildren().addAll(username, addExisting);

        HBox localActions = new HBox(9);
        Button createParticipant = new Button("＋ Create Participant");
        createParticipant.getStyleClass().add("secondary-button");
        Button demo = new Button("Add 3 Demo Members");
        demo.getStyleClass().add("secondary-button");
        Button remove = new Button("Remove Selected");
        remove.getStyleClass().add("danger-button");
        Button back = new Button("← Group");
        back.getStyleClass().add("ghost-button");
        createParticipant.setOnAction(e -> createParticipantDialog());
        demo.setOnAction(e -> {
            try {
                groupService.addDemoMembers(currentGroup.getId());
                info("Demo participants are ready. Existing members are not duplicated.");
                membersView();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        back.setOnAction(e -> groupDetails());

        ListView<Member> list = new ListView<>();
        list.getItems().addAll(members);
        list.setPrefHeight(Math.max(280, Math.min(490, members.size() * 54 + 45)));
        list.setPlaceholder(new Label("No participants yet. Create a local participant to begin."));
        remove.setOnAction(e -> removeSelectedMember(list));
        localActions.getChildren().addAll(createParticipant, demo, remove, back);

        VBox info = card(sectionIntro("Participants", members.size() + " member(s) in “" + currentGroup.getName() + "”. Local participants do not need their own login accounts."));
        content.getChildren().addAll(info, sectionIntro("Add an existing account", "Use this only when the person already has a SmartSplit login."), existingRow,
                sectionIntro("Or create participants locally", "Recommended for a classroom demo: add names directly without registering multiple accounts."), localActions, list);
        setContent(content, "Members — " + currentGroup.getName());
    }

    private void createParticipantDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Local Participant");
        styleDialogPane(dialog);
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(13);
        form.setPadding(new Insets(18));
        form.setMinWidth(420);
        TextField name = new TextField();
        name.setPromptText("Participant full name");
        TextField username = new TextField();
        username.setPromptText("Optional short identifier");
        form.addRow(0, new Label("Name"), name);
        form.addRow(1, new Label("Username (optional)"), username);
        Label note = new Label("A local participant can be used for expense allocation without logging in. Leave username empty to generate one automatically.");
        note.setWrapText(true);
        note.getStyleClass().add("hint");
        form.add(note, 0, 2, 2, 1);
        GridPane.setHgrow(name, Priority.ALWAYS);
        GridPane.setHgrow(username, Priority.ALWAYS);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("primary-button");
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    Member member = groupService.addGuestMember(currentGroup.getId(), name.getText(), username.getText());
                    info(member.getName() + " was added to the group.");
                    membersView();
                } catch (Exception ex) {
                    error(ex.getMessage());
                }
            }
        });
    }

    private void expensesView() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(28, 30, 34, 30));
        List<Expense> expenseRecords = expenseService.list(currentGroup.getId());
        Map<Integer, String> categories = categoryDAO.findAll();
        Map<Integer, String> memberNames = new LinkedHashMap<>();
        for (Member member : groupService.members(currentGroup.getId())) {
            memberNames.put(member.getId(), member.getName());
        }

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button add = new Button("＋  Add Expense");
        add.getStyleClass().add("primary-button");
        Button delete = new Button("Delete Selected");
        delete.getStyleClass().add("danger-button");
        Button back = new Button("← Group");
        back.getStyleClass().add("ghost-button");
        TextField search = new TextField();
        search.setPromptText("Search description or payer…");
        search.setPrefWidth(245);
        ComboBox<String> splitFilter = new ComboBox<>();
        splitFilter.getItems().addAll("All methods", "Equal", "Exact", "Percentage");
        splitFilter.getSelectionModel().selectFirst();
        splitFilter.setPrefWidth(150);
        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        toolbar.getChildren().addAll(add, delete, search, splitFilter, push, back);
        add.setOnAction(e -> addExpenseDialog());
        back.setOnAction(e -> groupDetails());

        TableView<Expense> table = new TableView<>();
        TableColumn<Expense, String> description = new TableColumn<>("Expense");
        description.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getDescription()));
        TableColumn<Expense, String> amount = new TableColumn<>("Amount");
        amount.setCellValueFactory(x -> new SimpleStringProperty(FormatUtil.shortMoney(x.getValue().getAmount())));
        TableColumn<Expense, String> paid = new TableColumn<>("Paid by");
        paid.setCellValueFactory(x -> new SimpleStringProperty(memberNames.getOrDefault(x.getValue().getPaidBy(), "Unknown")));
        TableColumn<Expense, String> category = new TableColumn<>("Category");
        category.setCellValueFactory(x -> new SimpleStringProperty(categories.getOrDefault(x.getValue().getCategoryId(), "Other")));
        TableColumn<Expense, String> split = new TableColumn<>("Split method");
        split.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getSplitType().toLowerCase(java.util.Locale.ROOT)));
        TableColumn<Expense, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getExpenseDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));
        table.getColumns().addAll(description, amount, paid, category, split, date);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(490);
        table.setPlaceholder(new Label("No expenses match this search. Add an expense or adjust the filters."));

        FilteredList<Expense> filtered = new FilteredList<>(FXCollections.observableArrayList(expenseRecords), item -> true);
        Runnable applyFilter = () -> {
            String query = search.getText() == null ? "" : search.getText().trim().toLowerCase(java.util.Locale.ROOT);
            String method = splitFilter.getValue();
            filtered.setPredicate(expense -> {
                String payer = memberNames.getOrDefault(expense.getPaidBy(), "").toLowerCase(java.util.Locale.ROOT);
                boolean matchesQuery = query.isEmpty() || expense.getDescription().toLowerCase(java.util.Locale.ROOT).contains(query) || payer.contains(query);
                boolean matchesMethod = method == null || method.equals("All methods") || expense.getSplitType().equalsIgnoreCase(method);
                return matchesQuery && matchesMethod;
            });
        };
        search.textProperty().addListener((obs, oldValue, newValue) -> applyFilter.run());
        splitFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter.run());
        table.setItems(filtered);
        delete.setOnAction(e -> deleteSelectedExpense(table));

        HBox summary = new HBox(14,
                stat("Expense records", String.valueOf(expenseRecords.size())),
                stat("Total group spending", FormatUtil.shortMoney(expenseService.totalSpent(currentGroup.getId()))));
        content.getChildren().addAll(sectionIntro("Expense ledger", "Search, filter and review every recorded group transaction."), summary, toolbar, table);
        setContent(content, "Expenses — " + currentGroup.getName());
    }

    private void deleteCurrentGroup() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        styleDialogPane(confirm);
        confirm.setTitle("Delete Group");
        confirm.setHeaderText("Delete “" + currentGroup.getName() + "”?");
        confirm.setContentText("This permanently deletes the group's expenses, settlements and membership records from this local database.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            groupService.deleteGroup(currentGroup.getId(), current.getId());
            currentGroup = null;
            groupsView();
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void removeSelectedMember(ListView<Member> list) {
        Member selected = list.getSelectionModel().getSelectedItem();
        if (selected == null) {
            error("Select a member first.");
            return;
        }
        if (selected.getId() == current.getId()) {
            error("You cannot remove yourself from the group.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        styleDialogPane(confirm);
        confirm.setTitle("Remove Member");
        confirm.setHeaderText("Remove “" + selected.getName() + "”?");
        confirm.setContentText("A member with existing expenses or settlements cannot be removed because it would corrupt the balance history.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            groupService.removeMember(currentGroup.getId(), selected.getId());
            membersView();
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void deleteSelectedExpense(TableView<Expense> table) {
        Expense selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            error("Select an expense first.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        styleDialogPane(confirm);
        confirm.setTitle("Delete Expense");
        confirm.setHeaderText("Delete “" + selected.getDescription() + "”?");
        confirm.setContentText("This removes the expense and its split records. Current balances will be recalculated, while previously recorded settlements remain in the history. Deleting old expenses can therefore change today's outstanding amounts.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            expenseService.delete(selected.getId());
            expensesView();
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private void addExpenseDialog() {
        List<Member> members = groupService.members(currentGroup.getId());
        if (members.isEmpty()) {
            error("Add at least one participant before recording an expense.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Expense");
        styleDialogPane(dialog);

        TextField description = new TextField();
        description.setPromptText("e.g. Dinner at the beach");
        TextField amount = new TextField();
        amount.setPromptText("0.00");
        ComboBox<Member> paidBy = new ComboBox<>();
        paidBy.getItems().addAll(members);
        paidBy.getSelectionModel().selectFirst();
        Map<Integer, String> categoryMap = categoryDAO.findAll();
        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll(categoryMap.values());
        category.getSelectionModel().selectFirst();
        ComboBox<String> split = new ComboBox<>();
        split.getItems().addAll("EQUAL", "EXACT", "PERCENTAGE");
        split.getSelectionModel().selectFirst();
        DatePicker date = new DatePicker(LocalDate.now());

        GridPane fields = new GridPane();
        fields.setHgap(12);
        fields.setVgap(12);
        fields.setPadding(new Insets(4, 0, 8, 0));
        fields.addRow(0, new Label("Expense description"), description);
        fields.addRow(1, new Label("Total amount (₹)"), amount);
        fields.addRow(2, new Label("Paid by"), paidBy);
        fields.addRow(3, new Label("Category"), category);
        fields.addRow(4, new Label("Split method"), split);
        fields.addRow(5, new Label("Expense date"), date);
        GridPane.setHgrow(description, Priority.ALWAYS);
        GridPane.setHgrow(amount, Priority.ALWAYS);
        GridPane.setHgrow(paidBy, Priority.ALWAYS);
        GridPane.setHgrow(category, Priority.ALWAYS);
        GridPane.setHgrow(split, Priority.ALWAYS);
        GridPane.setHgrow(date, Priority.ALWAYS);
        fields.setMinWidth(545);

        Label allocationHeading = new Label("Participant allocation");
        allocationHeading.getStyleClass().add("section-title");
        Label allocationHelp = new Label("Equal split divides the total automatically. Exact and percentage methods let you enter each participant's share.");
        allocationHelp.getStyleClass().add("section-subtitle");
        allocationHelp.setWrapText(true);
        VBox allocationRows = new VBox(8);
        Label validation = new Label("Enter a total amount to preview the allocation.");
        validation.getStyleClass().add("validation-neutral");
        Label preview = new Label();
        preview.setWrapText(true);
        preview.getStyleClass().add("row-copy");
        VBox allocationPanel = new VBox(10, allocationHeading, allocationHelp, allocationRows, validation, preview);
        allocationPanel.getStyleClass().add("allocation-panel");
        Map<Integer, TextField> allocationFields = new LinkedHashMap<>();
        boolean[] allocationValid = {false};

        Runnable refreshPreview = () -> allocationValid[0] = updateAllocationPreview(
                split.getValue(), amount.getText(), members, allocationFields, validation, preview);

        Runnable rebuildRows = () -> {
            allocationRows.getChildren().clear();
            allocationFields.clear();
            boolean inputRequired = !"EQUAL".equals(split.getValue());
            for (Member member : members) {
                Label name = new Label(member.getName());
                name.getStyleClass().add("allocation-name");
                TextField value = new TextField();
                value.setPrefWidth(160);
                value.setPromptText("PERCENTAGE".equals(split.getValue()) ? "0–100 %" : "Amount in ₹");
                value.setDisable(!inputRequired);
                HBox row = new HBox(12, name, value);
                row.getStyleClass().add("allocation-row");
                HBox.setHgrow(name, Priority.ALWAYS);
                allocationRows.getChildren().add(row);
                allocationFields.put(member.getId(), value);
                value.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
            }
            refreshPreview.run();
        };
        split.setOnAction(e -> rebuildRows.run());
        amount.textProperty().addListener((obs, oldValue, newValue) -> refreshPreview.run());
        rebuildRows.run();

        VBox body = new VBox(15, sectionIntro("Record a group expense", "The split preview updates as you enter values. The expense is saved only when all allocations are valid."), fields, allocationPanel);
        body.setPadding(new Insets(18));
        ScrollPane dialogScroll = new ScrollPane(body);
        dialogScroll.setFitToWidth(true);
        dialogScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dialogScroll.setPrefViewportHeight(520);
        dialogScroll.setPrefViewportWidth(610);
        dialogScroll.getStyleClass().add("content-scroll");
        dialog.getDialogPane().setContent(dialogScroll);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("primary-button");
        dialog.getDialogPane().setPrefWidth(700);

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) {
                return;
            }
            try {
                refreshPreview.run();
                if (!allocationValid[0]) {
                    error(validation.getText());
                    return;
                }
                Map<Integer, BigDecimal> values = new LinkedHashMap<>();
                if (!"EQUAL".equals(split.getValue())) {
                    for (Map.Entry<Integer, TextField> entry : allocationFields.entrySet()) {
                        values.put(entry.getKey(), new BigDecimal(entry.getValue().getText().trim()));
                    }
                }
                int categoryId = categoryMap.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(category.getValue()))
                        .map(Map.Entry::getKey).findFirst().orElseThrow(() -> new IllegalArgumentException("Choose a category."));
                expenseService.addExpense(currentGroup.getId(), description.getText(), amount.getText(),
                        paidBy.getValue().getId(), categoryId, date.getValue(), split.getValue(), members, values);
                info("Expense saved. Balances and reports will reflect the new transaction.");
                expensesView();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
    }

    private boolean updateAllocationPreview(String splitType, String amountText, List<Member> members,
            Map<Integer, TextField> fields, Label validation, Label preview) {
        try {
            if (amountText == null || amountText.trim().isEmpty()) {
                validation.setText("Enter a total amount to preview the allocation.");
                validation.getStyleClass().setAll("validation-neutral");
                preview.setText("");
                return false;
            }
            BigDecimal total = new BigDecimal(amountText.trim()).setScale(2, RoundingMode.HALF_UP);
            if (total.signum() <= 0) {
                throw new IllegalArgumentException("The total amount must be greater than zero.");
            }

            com.smartsplit.strategy.SplitStrategy strategy;
            Map<Integer, BigDecimal> values = new LinkedHashMap<>();
            if ("EQUAL".equals(splitType)) {
                strategy = new com.smartsplit.strategy.EqualSplitStrategy();
            } else {
                for (Map.Entry<Integer, TextField> entry : fields.entrySet()) {
                    String text = entry.getValue().getText();
                    if (text == null || text.trim().isEmpty()) {
                        validation.setText("Enter a value for every participant.");
                        validation.getStyleClass().setAll("validation-neutral");
                        preview.setText("");
                        return false;
                    }
                    values.put(entry.getKey(), new BigDecimal(text.trim()));
                }
                strategy = "EXACT".equals(splitType)
                        ? new com.smartsplit.strategy.ExactSplitStrategy()
                        : new com.smartsplit.strategy.PercentageSplitStrategy();
            }
            Map<Integer, BigDecimal> shares = strategy.calculate(total, members, values);
            StringBuilder summary = new StringBuilder();
            for (Member member : members) {
                if (summary.length() > 0) {
                    summary.append("   •   ");
                }
                summary.append(member.getName()).append(": ").append(FormatUtil.shortMoney(shares.get(member.getId())));
            }
            validation.setText("✓  Allocation balanced  •  ".concat(FormatUtil.shortMoney(total)));
            validation.getStyleClass().setAll("validation-valid");
            preview.setText(summary.toString());
            return true;
        } catch (Exception ex) {
            String message = ex.getMessage();
            validation.setText(message == null || message.isBlank() ? "Check the allocation values." : message);
            validation.getStyleClass().setAll("validation-invalid");
            preview.setText("");
            return false;
        }
    }

    private void balancesView() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(28, 30, 34, 30));
        List<Balance> balances = balanceService.balances(currentGroup.getId());
        BigDecimal creditors = BigDecimal.ZERO;
        BigDecimal debtors = BigDecimal.ZERO;
        for (Balance balance : balances) {
            if (balance.amount().signum() > 0) {
                creditors = creditors.add(balance.amount()); 
            }else {
                debtors = debtors.add(balance.amount().abs());
            }
        }
        HBox stats = new HBox(14,
                stat("Members", String.valueOf(balances.size())),
                stat("Members should receive", FormatUtil.shortMoney(creditors)),
                stat("Members owe", FormatUtil.shortMoney(debtors)));

        HBox actions = new HBox(10);
        Button suggest = new Button("Generate settlement plan  →");
        suggest.getStyleClass().add("primary-button");
        suggest.setOnAction(e -> settlementSuggestions());
        Button settlements = new Button("Open settlements");
        settlements.getStyleClass().add("secondary-button");
        settlements.setOnAction(e -> settlementsView());
        Button back = new Button("← Group");
        back.getStyleClass().add("ghost-button");
        back.setOnAction(e -> groupDetails());
        actions.getChildren().addAll(suggest, settlements, back);

        TableView<Balance> table = new TableView<>();
        TableColumn<Balance, String> name = new TableColumn<>("Participant");
        name.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().member().getName()));
        TableColumn<Balance, String> amount = new TableColumn<>("Net balance");
        amount.setCellValueFactory(x -> new SimpleStringProperty(FormatUtil.shortMoney(x.getValue().amount())));
        TableColumn<Balance, String> status = new TableColumn<>("Financial position");
        status.setCellValueFactory(x -> new SimpleStringProperty(
                x.getValue().amount().signum() > 0 ? "↑  Should receive" : x.getValue().amount().signum() < 0 ? "↓  Owes money" : "✓  Settled"));
        table.getColumns().addAll(name, amount, status);
        table.getItems().setAll(balances);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(470);
        table.setPlaceholder(new Label("No member balances to show yet."));
        VBox explanatory = card(sectionIntro("How to read balances", "A positive balance means this participant should receive money. A negative balance means they owe money. The total receivable and payable values should match."));
        content.getChildren().addAll(sectionIntro("Group balance overview", "Net balances are calculated from stored payments, expense shares, and completed settlements."), stats, actions, table, explanatory);
        setContent(content, "Balances — " + currentGroup.getName());
    }

    private void settlementSuggestions() {
        List<Settlement> suggestions = settlementService.suggestions(currentGroup.getId());
        if (suggestions.isEmpty()) {
            info("Everyone is settled. There are no payments to suggest.");
            return;
        }

        StringBuilder text = new StringBuilder();
        for (Settlement settlement : suggestions) {
            text.append(settlement.getPayerName()).append("  →  ")
                    .append(settlement.getReceiverName()).append("     ")
                    .append(FormatUtil.shortMoney(settlement.getAmount())).append("\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        styleDialogPane(alert);
        alert.setTitle("Settlement plan");
        alert.setHeaderText("Recommended payments");
        TextArea details = new TextArea(text.toString());
        details.setEditable(false);
        details.setWrapText(false);
        details.setPrefRowCount(Math.min(12, suggestions.size() + 2));
        details.getStyleClass().add("settlement-preview");
        alert.getDialogPane().setContent(new VBox(10, new Label("These payments simplify the current outstanding balances."), details));
        alert.showAndWait();
    }

    private void settlementsView() {
        VBox content = new VBox(17);
        content.setPadding(new Insets(28, 30, 34, 30));
        List<Settlement> history = settlementService.history(currentGroup.getId());
        BigDecimal pendingTotal = BigDecimal.ZERO;
        BigDecimal completedTotal = BigDecimal.ZERO;
        int pendingCount = 0;
        int completedCount = 0;
        for (Settlement settlement : history) {
            if ("PENDING".equals(settlement.getStatus())) {
                pendingTotal = pendingTotal.add(settlement.getAmount());
                pendingCount++;
            } else {
                completedTotal = completedTotal.add(settlement.getAmount());
                completedCount++;
            }
        }
        HBox stats = new HBox(14,
                stat("Pending payments", String.valueOf(pendingCount)),
                stat("Pending total", FormatUtil.shortMoney(pendingTotal)),
                stat("Completed payments", String.valueOf(completedCount)),
                stat("Completed total", FormatUtil.shortMoney(completedTotal)));

        HBox actions = new HBox(10);
        Button generate = new Button("Save current suggestions");
        generate.getStyleClass().add("primary-button");
        generate.setOnAction(e -> saveAllSuggestions());
        Button complete = new Button("Mark selected completed");
        complete.getStyleClass().add("secondary-button");
        Button back = new Button("← Group");
        back.getStyleClass().add("ghost-button");
        back.setOnAction(e -> groupDetails());

        TableView<Settlement> table = new TableView<>();
        TableColumn<Settlement, String> payer = new TableColumn<>("Payer");
        payer.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getPayerName()));
        TableColumn<Settlement, String> receiver = new TableColumn<>("Receiver");
        receiver.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getReceiverName()));
        TableColumn<Settlement, String> amount = new TableColumn<>("Amount");
        amount.setCellValueFactory(x -> new SimpleStringProperty(FormatUtil.shortMoney(x.getValue().getAmount())));
        TableColumn<Settlement, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));
        TableColumn<Settlement, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getStatus().equals("PENDING") ? "●  Pending" : "✓  Completed"));
        table.getColumns().addAll(payer, receiver, amount, date, status);
        table.getItems().setAll(history);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(470);
        table.setPlaceholder(new Label("No settlements have been saved yet. Generate suggestions from the Balances page."));

        complete.setOnAction(e -> {
            Settlement selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                error("Select a settlement first.");
                return;
            }
            if (!"PENDING".equals(selected.getStatus())) {
                error("This settlement is already completed.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(stage);
            styleDialogPane(confirm);
            confirm.setTitle("Complete settlement");
            confirm.setHeaderText("Mark this payment as completed?");
            confirm.setContentText(selected.getPayerName() + " → " + selected.getReceiverName() + "  •  " + FormatUtil.shortMoney(selected.getAmount()));
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
            try {
                settlementService.complete(selected.getId());
                info("Settlement marked completed.");
                settlementsView();
            } catch (Exception ex) {
                error(ex.getMessage());
            }
        });
        actions.getChildren().addAll(generate, complete, back);
        content.getChildren().addAll(sectionIntro("Settlement history", "Suggested payments can be saved as pending and marked completed after the payment is made."), stats, actions, table);
        setContent(content, "Settlements — " + currentGroup.getName());
    }

    private void saveAllSuggestions() {
        List<Settlement> suggestions = settlementService.suggestions(currentGroup.getId());
        if (suggestions.isEmpty()) {
            info("No outstanding settlements.");
            return;
        }

        List<Settlement> history = settlementService.history(currentGroup.getId());
        int saved = 0;
        for (Settlement s : suggestions) {
            boolean alreadyPending = history.stream().anyMatch(h
                    -> "PENDING".equals(h.getStatus())
                    && h.getPayerId() == s.getPayerId()
                    && h.getReceiverId() == s.getReceiverId()
                    && h.getAmount().compareTo(s.getAmount()) == 0);
            if (!alreadyPending) {
                settlementService.save(s);
                saved++;
            }
        }

        info(saved == 0
                ? "The current settlement suggestions are already saved as PENDING."
                : saved + " settlement suggestion(s) saved as PENDING.");
        settlementsView();
    }

    private void reportsView() {
        ReportService.ReportData report = reportService.summary(currentGroup.getId());
        VBox content = new VBox(18);
        content.setPadding(new Insets(28, 30, 34, 30));
        HBox stats = new HBox(14,
                stat("Participants", String.valueOf(report.memberCount())),
                stat("Expenses", String.valueOf(report.expenseCount())),
                stat("Total spent", FormatUtil.shortMoney(report.total())),
                stat("Average expense", FormatUtil.shortMoney(report.average())),
                stat("Largest expense", FormatUtil.shortMoney(report.largest())));

        PieChart pie = new PieChart();
        pie.setTitle("Spending by category");
        pie.setAnimated(false);
        pie.setLabelsVisible(true);
        Map<Integer, String> categories = categoryDAO.findAll();
        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Expense expense : expenseService.list(currentGroup.getId())) {
            String name = categories.getOrDefault(expense.getCategoryId(), "Other");
            byCategory.merge(name, expense.getAmount(), BigDecimal::add);
        }
        for (Map.Entry<String, BigDecimal> entry : byCategory.entrySet()) {
            pie.getData().add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
        }
        if (pie.getData().isEmpty()) {
            pie.setTitle("Spending by category — no expenses yet");
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Participant");
        yAxis.setLabel("Net balance (₹)");
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Participant balances");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        for (Balance balance : balanceService.balances(currentGroup.getId())) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(balance.member().getName(), balance.amount().doubleValue()));
        }
        chart.getData().add(series);
        chart.setPrefHeight(330);
        chart.setMinHeight(300);
        pie.setPrefHeight(330);
        pie.setMinHeight(300);
        HBox charts = new HBox(15, pie, chart);
        charts.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(pie, Priority.ALWAYS);
        HBox.setHgrow(chart, Priority.ALWAYS);
        pie.setPrefWidth(420);
        pie.setMinWidth(360);
        chart.setPrefWidth(560);
        chart.setMinWidth(440);

        VBox insights = card(sectionIntro("Report notes", "This view is generated from saved SQLite expense records using Java ReportService, the category ledger, and BalanceService."));
        Label note = new Label("Use this report to explain the total spending, average transaction size, category distribution, and each participant's current net financial position.");
        note.getStyleClass().add("muted");
        note.setWrapText(true);
        insights.getChildren().add(note);
        Button back = new Button("← Group");
        back.getStyleClass().add("ghost-button");
        back.setOnAction(e -> groupDetails());
        content.getChildren().addAll(sectionIntro("Group analytics", "A readable summary of expense patterns and outstanding balances for “" + currentGroup.getName() + "”."), stats, charts, insights, back);
        setContent(content, "Reports — " + currentGroup.getName());
    }

    private void loadDemoData() {
        try {
            Group demoGroup = groupService.list(current.getId()).stream()
                    .filter(group -> "SmartSplit Demo Trip".equalsIgnoreCase(group.getName()))
                    .findFirst().orElse(null);
            if (demoGroup == null) {
                int id = groupService.create("SmartSplit Demo Trip", "Ready-made demonstration group", current.getId());
                groupService.addMember(id, current.getId());
                demoGroup = groupService.get(id);
            }
            groupService.addDemoMembers(demoGroup.getId());
            List<Member> members = groupService.members(demoGroup.getId());
            boolean seeded = expenseService.list(demoGroup.getId()).isEmpty();
            if (seeded && members.size() >= 2) {
                Map<Integer, String> cats = categoryDAO.findAll();
                int food = categoryId(cats, "Food");
                int travel = categoryId(cats, "Travel");
                int accommodation = categoryId(cats, "Accommodation");
                expenseService.addExpense(demoGroup.getId(), "Group Dinner", "2400", members.get(0).getId(), food,
                        LocalDate.now().minusDays(2), "EQUAL", members, Map.of());

                Map<Integer, BigDecimal> exact = new LinkedHashMap<>();
                BigDecimal exactTotal = new BigDecimal("800.00");
                BigDecimal exactBase = exactTotal.divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.DOWN);
                BigDecimal assigned = BigDecimal.ZERO;
                for (int i = 0; i < members.size(); i++) {
                    BigDecimal value = i == members.size() - 1 ? exactTotal.subtract(assigned) : exactBase;
                    exact.put(members.get(i).getId(), value);
                    assigned = assigned.add(value);
                }
                expenseService.addExpense(demoGroup.getId(), "Taxi", "800", members.get(1 % members.size()).getId(), travel,
                        LocalDate.now().minusDays(1), "EXACT", members, exact);

                Map<Integer, BigDecimal> percentages = new LinkedHashMap<>();
                BigDecimal percentBase = new BigDecimal("100").divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.DOWN);
                BigDecimal percentAssigned = BigDecimal.ZERO;
                for (int i = 0; i < members.size(); i++) {
                    BigDecimal value = i == members.size() - 1 ? new BigDecimal("100").subtract(percentAssigned) : percentBase;
                    percentages.put(members.get(i).getId(), value);
                    percentAssigned = percentAssigned.add(value);
                }
                expenseService.addExpense(demoGroup.getId(), "Hotel", "4000", members.get(2 % members.size()).getId(), accommodation,
                        LocalDate.now(), "PERCENTAGE", members, percentages);
            }
            currentGroup = demoGroup;
            info(seeded ? "Demo workspace created with equal, exact and percentage examples." : "Opened your existing demo workspace. Existing expenses were kept unchanged.");
            groupDetails();
        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    private int categoryId(Map<Integer, String> categories, String name) {
        return categories.entrySet().stream()
                .filter(e -> e.getValue().equalsIgnoreCase(name))
                .map(Map.Entry::getKey)
                .findFirst().orElse(1);
    }

    private void info(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        styleDialogPane(alert);
        alert.setTitle("SmartSplit");
        alert.setHeaderText("Completed");
        alert.setContentText(msg == null ? "The operation completed." : msg);
        alert.showAndWait();
    }

    private void error(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (stage != null) {
            alert.initOwner(stage);
        }
        styleDialogPane(alert);
        alert.setTitle("SmartSplit");
        alert.setHeaderText("Something needs attention");
        alert.setContentText(msg == null || msg.isBlank() ? "Unexpected error. Please check the input and try again." : msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
