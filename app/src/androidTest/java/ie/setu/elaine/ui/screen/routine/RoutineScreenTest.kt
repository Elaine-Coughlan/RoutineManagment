package ie.setu.elaine.ui.screen.routine

import androidx.compose.ui.test.junit4.createComposeRule
import ie.setu.elaine.viewmodel.RoutineViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class RoutineScreenTest {

    @get:Rule
    val composableTestRule = createComposeRule()

    //Mock viewmodel and dependencies - needed with our MVVM architecture
    private val mockViewModel = mock(RoutineViewModel::class.java)
    private val mockOnRoutineClick: (String) -> Unit = mock(Function1::class.java) as (String) -> Unit
    private val mockOnAddRoutineClick: () -> Unit = mock(Function0::class.java) as () -> Unit

    @Test
    fun routineCreationTest(){
        composableTestRule.setContent{
            RoutineListScreen(
                viewModel = mockViewModel,
                onRoutineClick = mockOnRoutineClick,
                onAddRoutineClick = mockOnAddRoutineClick
            )
        }
    }
}