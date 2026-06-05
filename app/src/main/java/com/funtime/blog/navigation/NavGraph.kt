package com.funtime.blog.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.funtime.blog.ui.article.ArticleDetailScreen
import com.funtime.blog.ui.auth.AuthScreen
import com.funtime.blog.ui.author.AuthorScreen
import com.funtime.blog.ui.bookmark.BookmarkScreen
import com.funtime.blog.ui.category.CategoryArticleListScreen
import com.funtime.blog.ui.category.CategoryScreen
import com.funtime.blog.ui.home.HomeScreen
import com.funtime.blog.ui.passport.PassportScreen
import com.funtime.blog.ui.profile.ProfileScreen
import com.funtime.blog.ui.search.SearchScreen
import java.net.URLEncoder

private val topLevelRoutes = setOf("home", "categories", "bookmarks", "profile")

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "首頁") },
                        label = { Text("首頁") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "categories",
                        onClick = {
                            navController.navigate("categories") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = "分類") },
                        label = { Text("分類") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "bookmarks",
                        onClick = {
                            navController.navigate("bookmarks") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "書籤") },
                        label = { Text("書籤") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                        label = { Text("我的") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onArticleClick = { slug -> navController.navigate("article/$slug") },
                    onSearchClick = { navController.navigate("search") }
                )
            }
            composable("categories") {
                CategoryScreen(
                    onCategoryClick = { theme, category ->
                        val t = URLEncoder.encode(theme, "UTF-8")
                        val c = URLEncoder.encode(category, "UTF-8")
                        navController.navigate("category-articles/$t/$c")
                    }
                )
            }
            composable(
                route = "search?keyword={keyword}",
                arguments = listOf(navArgument("keyword") { type = NavType.StringType; defaultValue = "" })
            ) {
                SearchScreen(
                    onArticleClick = { slug -> navController.navigate("article/$slug") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("bookmarks") {
                BookmarkScreen(
                    onArticleClick = { slug -> navController.navigate("article/$slug") }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onLoginClick = { navController.navigate("login") },
                    onArticleClick = { slug -> navController.navigate("article/$slug") },
                    onPassportClick = { navController.navigate("passport") }
                )
            }
            composable("passport") {
                PassportScreen(onBack = { navController.popBackStack() })
            }
            composable("login") {
                AuthScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable(
                route = "article/{slug}",
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) {
                ArticleDetailScreen(
                    onBack = { navController.popBackStack() },
                    onAuthorClick = { slug -> navController.navigate("author/$slug") },
                    onTagClick = { keyword ->
                        navController.navigate(
                            "search?keyword=" + URLEncoder.encode(keyword, "UTF-8")
                        )
                    }
                )
            }
            composable(
                route = "category-articles/{theme}/{category}",
                arguments = listOf(
                    navArgument("theme") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType }
                )
            ) {
                CategoryArticleListScreen(
                    onBack = { navController.popBackStack() },
                    onArticleClick = { slug -> navController.navigate("article/$slug") }
                )
            }
            composable(
                route = "author/{authorSlug}",
                arguments = listOf(navArgument("authorSlug") { type = NavType.StringType })
            ) {
                AuthorScreen(
                    onBack = { navController.popBackStack() },
                    onArticleClick = { slug -> navController.navigate("article/$slug") }
                )
            }
        }
    }
}
