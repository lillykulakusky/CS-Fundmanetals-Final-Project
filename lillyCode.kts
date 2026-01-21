import khoury.input
import khoury.reactConsole
import khoury.CapturedResult
import khoury.EnabledTest
import khoury.runEnabledTests
import khoury.testSame
import khoury.fileReadAsString

// Data structure for question and answer
data class Question(val questionText: String, val answerText: String)

// Data structure for question bank
data class QuestionBank(val name: String, val questions: List<Question>)

// Immutable state for question bank quiz
data class QuestionBankState(
    val questionBank: QuestionBank,
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val showAnswer: Boolean = false
)

// Function to load questions from a file and categorize them
fun loadQuestionsFromFile(path: String): List<QuestionBank> {
    val banks = mutableMapOf<String, MutableList<Question>>()
    var currentBank = "pop culture" // Default bank if no heading is found

    fileReadAsString(path).lines().forEach { line ->
        if (line.startsWith("#")) {
            currentBank = line.removePrefix("#").trim()
            banks.putIfAbsent(currentBank, mutableListOf())
        } else if (line.contains("|")) {
            val (questionText, answerText) = line.split("|")
            banks[currentBank]?.add(Question(questionText.trim(), answerText.trim()))
        }
    }
    return banks.map { QuestionBank(it.key, it.value) }
}

// Prompts user to select a question bank
fun chooseBank(banks: List<QuestionBank>): QuestionBank {
    println("Select a question bank:")
    banks.forEachIndexed { index, bank -> println("${index + 1}. ${bank.name}") }
    val choice = input().toIntOrNull()
    return banks.getOrNull(choice?.minus(1) ?: -1) ?: chooseBank(banks)
}

// Determines correctness based on user input
fun isCorrect(input: String): Boolean = input.lowercase().startsWith("y")

// Function to create a new state after studying each question
fun studyQuestion(state: QuestionBankState, unused: String): QuestionBankState { // Renamed parameter
    val question = state.questionBank.questions[state.currentIndex]
    return if (!state.showAnswer) {
        println("Q: ${question.questionText}\nPress Enter to reveal the answer...")
        input()
        state.copy(showAnswer = true)
    } else {
        println("Did you get it right? (y/n)")
        val updatedCorrectCount = if (isCorrect(input())) state.correctCount + 1 else state.correctCount
        state.copy(
            currentIndex = state.currentIndex + 1,
            correctCount = updatedCorrectCount,
            showAnswer = false
        )
    }
}


// Checks if quiz should terminate
fun terminate(state: QuestionBankState): Boolean = state.currentIndex >= state.questionBank.questions.size

// Function to return final message when quiz ends
fun terminalStateToText(state: QuestionBankState): String {
    return "Quiz complete! You got ${state.correctCount} out of ${state.questionBank.questions.size} correct."
}

// Main function to go through chosen question bank
fun studyQuestionBank(bank: QuestionBank): Int {
    val initialState = QuestionBankState(bank)
    return reactConsole(
        initialState,
        { state -> if (!state.showAnswer) "Q: ${state.questionBank.questions[state.currentIndex].questionText}" else "A: ${state.questionBank.questions[state.currentIndex].answerText}" },
        ::studyQuestion,
        ::terminate,
        ::terminalStateToText
    ).correctCount
}

// Testing functions using `@EnabledTest`
@EnabledTest
fun testLoadQuestionsFromFile() {
    val result = loadQuestionsFromFile("questions.txt")
    assert(result.size == 3) // Expecting 3 banks
    assert(result[0].questions.isNotEmpty()) // Ensure questions were loaded
}

@EnabledTest
fun testChooseBank() {
    val banks = listOf(
        QuestionBank("Test Bank 1", listOf(Question("Q1?", "A1"))),
        QuestionBank("Test Bank 2", listOf(Question("Q2?", "A2")))
    )
    val result: CapturedResult<QuestionBank> = khoury.captureResults({ chooseBank(banks) }, "1") // Correctly pass lambda and input
    assert(result.returnedValue?.name == "Test Bank 1") // Properly handle nullable `returnedValue`
}

@EnabledTest
fun testStudyQuestion() {
    val bank = QuestionBank("Test Bank", listOf(Question("Q1?", "A1")))
    val state = QuestionBankState(bank)
    val resultState = studyQuestion(state, "") // Pass dummy string for compatibility
    assert(resultState.showAnswer) // After first call, answer should show
}

// Load questions, choose bank, and start quiz
fun main() {
    val questionBanks = loadQuestionsFromFile("questions.txt")
    val chosenBank = chooseBank(questionBanks)
    val score = studyQuestionBank(chosenBank)
    println("Final Score: $score out of ${chosenBank.questions.size}")

    // Run all enabled testsf
    runEnabledTests(this)
}

main()
