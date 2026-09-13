package com.example.doline.views.screens.store

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.doline.R
import com.example.doline.ui.theme.FontSize
import com.example.doline.ui.theme.IconSize
import com.example.doline.ui.theme.Spacing
import com.example.doline.views.screens.store.home.ChatScreen
import com.example.doline.views.screens.store.home.ChatScreenViewModel
import com.example.doline.views.screens.store.home.CreateExpenseScreen
import com.example.doline.views.screens.store.home.CreateExpenseViewModel
import com.example.doline.views.screens.store.home.CreateNoteScreen
import com.example.doline.views.screens.store.home.CreateNoteScreenViewModel
import com.example.doline.views.screens.store.home.ExpenseViewModel
import com.example.doline.views.screens.store.home.ExpensesScreen
import com.example.doline.views.screens.store.home.FaqsScreen
import com.example.doline.views.screens.store.home.FeedbackScreen
import com.example.doline.views.screens.store.home.HelpScreen
import com.example.doline.views.screens.store.home.InboxScreen
import com.example.doline.views.screens.store.home.InboxScreenViewModel
import com.example.doline.views.screens.store.home.NotesScreen
import com.example.doline.views.screens.store.home.NotesScreenViewModel
import com.example.doline.views.screens.store.home.ReportsScreen
import com.example.doline.views.screens.store.home.ReportsScreenViewModel
import com.example.doline.views.screens.store.home.SearchHelpScreen
import com.example.doline.views.screens.store.home.StoreDashBoardScreen
import com.example.doline.views.screens.store.inventory.BatchDetailScreen
import com.example.doline.views.screens.store.inventory.BatchDetailsViewModel
import com.example.doline.views.screens.store.inventory.ItemForm
import com.example.doline.views.screens.store.inventory.EditItemScreenViewModel
import com.example.doline.views.screens.store.inventory.Inventory
import com.example.doline.views.screens.store.inventory.InventoryViewModel
import com.example.doline.views.screens.store.inventory.ItemScreen
import com.example.doline.views.screens.store.inventory.ItemViewModel
import com.example.doline.views.screens.store.inventory.RestockScreen
import com.example.doline.views.screens.store.inventory.RestockViewModel
import com.example.doline.views.screens.store.inventory.SearchInventoryScreen
import com.example.doline.views.screens.store.home.settings.CreateDeliveryZone
import com.example.doline.views.screens.store.home.settings.CreateStaffScreen
import com.example.doline.views.screens.store.home.settings.GeneralSettingsScreen
import com.example.doline.views.screens.store.home.settings.InventorySettingsScreen
import com.example.doline.views.screens.store.home.settings.NotificationSettingsScreen
import com.example.doline.views.screens.store.home.settings.PaymentSettingsScreen
import com.example.doline.views.screens.store.home.settings.PosSettingsScreen
import com.example.doline.views.screens.store.home.settings.ReportingSettingsScreen
import com.example.doline.views.screens.store.home.settings.SettingsScreen
import com.example.doline.views.screens.store.home.settings.ShippingSettingsScreen
import com.example.doline.views.screens.store.home.settings.StaffSettingsScreen
import com.example.doline.views.screens.store.home.settings.TaxationSettingsScreen
import com.example.doline.views.screens.store.home.settings.ScreenViewModel
import com.example.doline.views.screens.store.orders.OrderScreen
import com.example.doline.views.screens.store.orders.OrderScreenViewModel
import com.example.doline.views.screens.store.orders.OrdersScreen
import com.example.doline.views.screens.store.orders.OrdersScreenViewModel
import com.example.doline.views.screens.store.pos.PosScreen
import com.example.doline.views.screens.store.pos.PosScreenViewModel

data class TabItem(
    val route: String,           // Unique route for navigation
    val title: String,           // Display name on the tab
    val icon: Int,       // Icon for the bottom navigation
    val filledIcon: ImageVector? = null  // Optional: filled version when selected (for better UX)
)


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StoreMain(storeId: Long?){
    val storeNavController = rememberNavController()

    if(storeId == null) {
        return
    }

    // Tabs
    val tabs = listOf(
        TabItem("store", "Store", R.drawable.dashboard),
        TabItem("$storeId/inventory", "Inventory", R.drawable.inventory),
        TabItem("$storeId/pos", "POS", R.drawable.pos),
        TabItem("$storeId/orders", "Orders", R.drawable.orders),
        TabItem("$storeId/lilli", "Lilli", R.drawable.lilli)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = colorScheme.background,
                contentColor = colorScheme.onBackground,
                modifier = Modifier.shadow(Spacing.MD)
            ) {
                val currentRoute = storeNavController.currentBackStackEntryAsState().value?.destination?.route

                tabs.forEach { tab ->

                    val isSelected = if(tab.route == "store")
                        currentRoute?.startsWith(tab.route)
                    else
                        currentRoute?.startsWith("{storeId}/${tab.title.lowercase()}")

                    NavigationBarItem(
                        selected = isSelected ?: false,
                        onClick = {
                            storeNavController.navigate(tab.route) {
                                popUpTo(storeNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        alwaysShowLabel = true,
                        label = {
                            Text(
                                tab.title,
                                fontWeight = FontWeight.W900,
                                fontSize = FontSize.XXS
                            )
                                },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.icon),
                                contentDescription = tab.title,
                                modifier = Modifier.size(IconSize.NORMAL),
                            )
                        },
                        colors = NavigationBarItemColors(
                            selectedIconColor = colorScheme.primary,
                            unselectedIconColor = colorScheme.onBackground,
                            selectedTextColor = colorScheme.primary,
                            unselectedTextColor = colorScheme.onBackground,
                            selectedIndicatorColor = Color.Transparent,
                            disabledIconColor = colorScheme.onBackground.copy(.4f),
                            disabledTextColor = colorScheme.onBackground.copy(.4f)
                        )
                    )
                }
            }
        }
    ) { p ->
        p.calculateTopPadding()
        NavHost(
            navController = storeNavController,
            startDestination = "store"
        ){
            // Dashboard destinations
            composable(
                "store"
            ) {
                StoreDashBoardScreen(
                    navController = storeNavController,
                    storeId = storeId
                )
            }

            composable(
                "store/{storeId}/reports",
                listOf(navArgument("storeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewmodel: ReportsScreenViewModel = hiltViewModel(backStackEntry)
                ReportsScreen(navController = storeNavController, viewmodel)
            }

            composable(
                "store/{storeId}/inbox",
                listOf(navArgument("storeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: InboxScreenViewModel = hiltViewModel(backStackEntry)
                InboxScreen(navController = storeNavController, viewModel)
            }

            composable(
                "store/{storeId}/inbox/{clientId}",
                listOf(navArgument("storeId") { type = NavType.LongType }, navArgument("clientId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: ChatScreenViewModel = hiltViewModel(backStackEntry)
                ChatScreen(navController = storeNavController, viewModel)
            }

            composable(
                "store/{storeId}/expenses",
                listOf(navArgument("storeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: ExpenseViewModel = hiltViewModel(backStackEntry)
                ExpensesScreen(navController = storeNavController, viewModel)
            }

            composable(
                "store/{storeId}/expenses/create",
                listOf(navArgument("storeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: CreateExpenseViewModel = hiltViewModel(backStackEntry)
                CreateExpenseScreen(navController = storeNavController, viewModel)
            }

            composable(
                "store/{storeId}/notes",
                listOf(navArgument("storeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: NotesScreenViewModel = hiltViewModel(backStackEntry)
                NotesScreen(navController = storeNavController, viewModel)
            }

            composable(
                "store/{storeId}/notes/create",
                listOf(navArgument("storeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewmodel: CreateNoteScreenViewModel = hiltViewModel(backStackEntry)
                CreateNoteScreen(storeNavController, viewmodel)
            }

            composable(
                "store/{storeId}/settings",
                listOf(navArgument("storeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: ScreenViewModel = hiltViewModel(backStackEntry)
                SettingsScreen(storeNavController, viewModel)
            }

            composable("store/{storeId}/settings/general") { GeneralSettingsScreen(navController = storeNavController) }
            composable("store/{storeId}/settings/notifications") { NotificationSettingsScreen(navController = storeNavController) }
            composable("store/{storeId}/settings/inventory") { InventorySettingsScreen(navController = storeNavController) }
            composable("store/{storeId}/settings/reports_&_analytics") { ReportingSettingsScreen(navController = storeNavController) }
            composable("store/{storeId}/settings/payments_&_tenders") { PaymentSettingsScreen(navController = storeNavController) }
            composable("store/{storeId}/settings/shipping_&_deliveries") { ShippingSettingsScreen(navController = storeNavController) }
            composable("store/{storeId}/settings/shipping_&_deliveries/create_zone"){ CreateDeliveryZone(navController = storeNavController) }
            composable("store/{storeId}/settings/taxation") { TaxationSettingsScreen(navController = storeNavController) }
            composable("store/{storeId}/settings/staff_&_security") { StaffSettingsScreen(navController = storeNavController) }
            composable("store/{storeId}/settings/staff_&_security/create_staff") { CreateStaffScreen(navController = storeNavController) }
            composable("store/{storeId}/settings/pos_hardware") { PosSettingsScreen(navController = storeNavController) }
            composable("store/{storeId}/help") { HelpScreen(navController = storeNavController) }
            composable("store/{storeId}/help/feedback") { FeedbackScreen(navController = storeNavController) }
            composable("store/{storeId}/help/search") { SearchHelpScreen(navController = storeNavController) }
            composable("store/{storeId}/help/faqs/{faqCategory}", arguments = listOf(navArgument("faqCategory") { type = NavType.StringType })) { backStackEntry ->
                FaqsScreen(
                    navController = storeNavController,
                    faqCategory = backStackEntry.arguments?.getString("faqCategory")
                )
            }

            // Inventory destinations
            composable(
                "{storeId}/inventory",
                listOf(navArgument("storeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: InventoryViewModel = hiltViewModel(backStackEntry)
                Inventory(navController = storeNavController, viewModel)
            }

            composable(
                "{storeId}/inventory/search"
            ) { backStackEntry ->
                val viewModel: InventoryViewModel = hiltViewModel(backStackEntry)
                SearchInventoryScreen(navController = storeNavController, viewModel)
            }

            composable(
                "{storeId}/inventory/{itemId}",
                arguments = listOf(
                    navArgument("itemId"){ type = NavType.LongType},
                    navArgument("storeId"){ type = NavType.LongType}
                )
            ) { backStackEntry ->
                val viewModel: ItemViewModel = hiltViewModel(backStackEntry)
                ItemScreen(navController = storeNavController, viewModel)
            }

            composable(
                "{storeId}/inventory/new-item",
                listOf(navArgument("storeId"){type = NavType.LongType})
            ) { backStackEntry ->
                val viewmodel: EditItemScreenViewModel = hiltViewModel(backStackEntry)
                ItemForm(navController = storeNavController, viewmodel)
            }

            composable(
                "{storeId}/inventory/restock/{itemId}",
                listOf(
                    navArgument("itemId"){type = NavType.LongType},
                    navArgument("storeId"){ type = NavType.LongType}
                )
            ) { backStackEntry ->
                val viewmodel: RestockViewModel = hiltViewModel(backStackEntry)
                RestockScreen(navController = storeNavController, viewmodel)
            }

            composable(
                "{storeId}/inventory/{itemId}/batch-details/{batchId}",
                arguments = listOf(
                    navArgument("storeId"){type = NavType.LongType},
                    navArgument("itemId"){type = NavType.LongType},
                    navArgument("batchId"){type = NavType.LongType}
                )
            ) { backStackEntry ->
                val viewmodel: BatchDetailsViewModel = hiltViewModel(backStackEntry)
                BatchDetailScreen(navController = storeNavController, viewmodel = viewmodel)
            }

            composable(
                "{storeId}/inventory/edit-item/{itemId}",
                arguments = listOf(
                    navArgument("itemId"){type = NavType.LongType},
                    navArgument("storeId"){type = NavType.LongType}
                )
            ) { backStackEntry ->
                val viewmodel: EditItemScreenViewModel = hiltViewModel(backStackEntry)
                ItemForm(navController = storeNavController, viewmodel)
            }

            // POS destinations
            composable(
                "{storeId}/pos",
                arguments = listOf(
                    navArgument("storeId"){type = NavType.LongType}
                )
            ) { backStackEntry ->
                val viewModel: PosScreenViewModel = hiltViewModel(backStackEntry)
                PosScreen(navController = storeNavController, viewModel)
            }

            // Orders destinations
            composable(
                route = "{storeId}/orders",
                arguments = listOf(
                    navArgument("storeId"){ type = NavType.LongType}
                )
            ) { b ->
                val viewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<OrdersScreenViewModel>(b)
                OrdersScreen(navController = storeNavController, viewModel)
            }
            composable(
                route = "{storeId}/orders/{orderId}",
                arguments = listOf(
                    navArgument("storeId"){ type = NavType.LongType},
                    navArgument("orderId"){ type = NavType.LongType}
                )
            ) { b ->
                val viewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<OrderScreenViewModel>(b)
                OrderScreen(navController = storeNavController, viewModel)
            }

            // Ai assistant (Asubo)
            composable("{storeId}/lilli") { LilliScreen() }
        }
    }

}
