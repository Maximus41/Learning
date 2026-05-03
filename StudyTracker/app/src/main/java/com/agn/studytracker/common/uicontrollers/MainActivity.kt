package com.agn.studytracker.common.uicontrollers

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agn.studytracker.sessions.ui.AssessSessionScreen
import com.agn.studytracker.sessions.ui.EditSessionScreen
import com.agn.studytracker.sessions.ui.SessionsScreen
import com.agn.studytracker.sessions.ui.UpdateSessionScreen
import com.agn.studytracker.subjects.ui.SubjectDetailsScreen
import com.agn.studytracker.subjects.ui.SubjectsScreen
import com.agn.studytracker.ui.theme.StudyTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyTrackerTheme {
                StudyTrackerNavHost()
            }
        }
    }
}

@Composable
private fun StudyTrackerNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "subjects") {

        composable("subjects") {
            SubjectsScreen(
                onNavigateToSessions = { subjectId ->
                    navController.navigate("sessions/$subjectId")
                },
                onNavigateToDetails = { subjectId, subjectName ->
                    navController.navigate("subjectDetails/$subjectId/${Uri.encode(subjectName)}")
                }
            )
        }

        composable("subjectDetails/{subjectId}/{subjectName}") { backStack ->
            val subjectId = backStack.arguments?.getString("subjectId") ?: ""
            val subjectName = Uri.decode(backStack.arguments?.getString("subjectName") ?: "")
            SubjectDetailsScreen(
                subjectId = subjectId,
                subjectName = subjectName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("sessions/{subjectId}") { backStack ->
            val subjectId = backStack.arguments?.getString("subjectId") ?: ""
            SessionsScreen(
                subjectId = subjectId,
                onNavigateToUpdate = { sessionId ->
                    navController.navigate("updateSession/$sessionId")
                },
                onNavigateToEdit = { sessionId ->
                    navController.navigate("editSession/$sessionId")
                },
                onNavigateToAssess = { sessionId ->
                    navController.navigate("assessSession/$sessionId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("editSession/{sessionId}") { backStack ->
            val sessionId = backStack.arguments?.getString("sessionId") ?: ""
            EditSessionScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("updateSession/{sessionId}") { backStack ->
            val sessionId = backStack.arguments?.getString("sessionId") ?: ""
            UpdateSessionScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("assessSession/{sessionId}") { backStack ->
            val sessionId = backStack.arguments?.getString("sessionId") ?: ""
            AssessSessionScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
