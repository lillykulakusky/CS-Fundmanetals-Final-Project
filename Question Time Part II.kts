/*** Step 1: Questions ***/

val QASEPARATOR  = "|"
val TAGSEPARATOR = ","

/*** Step 2: Files of tagged questions ***/

/*** Step 3: Question bank design ***/

/** 
 * The bank is either completed, 
 * showing a question or showing
 * an answer
 */
enum class QuestionBankState { COMPLETED, QUESTIONING, ANSWERING }

/**
 * Basic functionality of any question bank
 */
interface IQuestionBank {

    /**
     * Returns the state of a question bank.
     */
    fun getState(): QuestionBankState

    /**
     * Returns the currently visible text (or null if completed).
     */
    fun getText(): String?

    /**
     * Returns the number of question-answer pairs.
     * (Size does not change when a question is put
     * to the end of the question bank.)
     */
    fun getSize(): Int

    /**
     * Shifts from question to answer. If not QUESTIONING,
     * returns the same IQuestionBank.
     */
    fun show(): IQuestionBank

    /**
     * Shifts from an answer to the next question (or completion).
     * If the current question was answered correctly, it discards
     * it. Otherwise it cycles the question to the end.
     * 
     * If not ANSWERING, returns the same IQuestionBank.
     */
    fun next(correct: Boolean): IQuestionBank
}

/*** Step 4: Menu design ***/

/**
 * The only required capability for a menu option
 * is to be able to render its title.
 */
interface IMenuOption {
    fun getTitle(): String
}

/**
 * A menu option with a single value and name.
 */
data class NamedMenuOption<T>(
    val option: T,
    val title: String
) : IMenuOption {
    
    override fun getTitle(): String = title
}

fun <T : IMenuOption> chooseMenu(options: List<T>): T? {
    /**
     * Your code here.
     * 
     * Call reactConsole (with appropriate handlers)
     * and return the selected option (or null).
     */
}

@EnabledTest
fun testChooseMenu() {

    /**
     * Individual examples, as well as a list of those examples
     * (for testing purposes only)
     */
    val anApple = NamedMenuOption(1, "Apple")
    val aBanana = NamedMenuOption(2, "Banana")
    val fruits = listOf(anApple, aBanana)

    /* Some useful outputs */
    val prompt = "Enter 1, 2, ..., or 0 to quit:"
    val quit   = "Bye."

    testSame(
        captureResults(
            { chooseMenu(listOf(anApple)) },
            "",
            "0",
        ),
        CapturedResult(
            null,
            "1. ${anApple.name}",
            "",
            prompt,
            "1. ${anApple.name}",
            "",
            prompt,
            quit,
        ),
        "Quitting",
    )

    testSame(
        captureResults(
            { chooseMenuOption(fruits) },
            "",
            "10",
            "-3",
            "1",
        ),
        CapturedResult(
            anApple,
            "1. ${anApple.name}", 
            "2. ${aBanana.name}", 
            "", 
            prompt,
            "1. ${anApple.name}", 
            "2. ${aBanana.name}", 
            "", 
            prompt,
            "1. ${anApple.name}", 
            "2. ${aBanana.name}", 
            "", 
            prompt,
            "1. ${anApple.name}", 
            "2. ${aBanana.name}", 
            "", 
            menuPrompt,
            "You chose to study ${anApple.name}",
        ),
        "Choose Bank #1",
    )

    testSame(
        captureResults(
            { chooseMenuOption(fruits) },
            "3",
            "2",
        ),
        CapturedResult(
            aBanana,
            "1. ${anApple.name}", 
            "2. ${aBanana.name}", 
            "", 
            prompt,
            "1. ${anApple.name}", 
            "2. ${aBanana.name}", 
            "", 
            prompt,
            "You chose to study ${aBanana.name}",
        ),
        "Choose Bank #2",
    )
}

/*** Step 5: Sentiment analysis ***/

typealias Classifier = (String) -> Boolean

data class LabeledExample<E, L>(val example: E, val label: L)

val dataset: List<LabeledExample<String, Boolean>> =
    listOf(
        /* Some positive examples */
        LabeledExample("yes", true),
        LabeledExample("y", true),
        LabeledExample("indeed", true),
        LabeledExample("aye", true),
        LabeledExample("oh yes", true),
        LabeledExample("affirmative", true),
        LabeledExample("roger", true),
        LabeledExample("uh huh", true),
        LabeledExample("true", true),
        /* Some negative examples */
        LabeledExample("no", false),
        LabeledExample("n", false),
        LabeledExample("nope", false),
        LabeledExample("negative", false),
        LabeledExample("nay", false),
        LabeledExample("negatory", false),
        LabeledExample("uh uh", false),
        LabeledExample("absolutely not", false),
        LabeledExample("false", false),
    )

/**
 * Heuristically determines if the supplied string
 * is positive based on the first letter being Y!
 *
 * This is our naive classifier.
 */
fun naiveClassifier(s: String): Boolean = s.uppercase().startsWith("Y")

/**
 * Tests whether our classifier returns the expected result
 * for an element of our data set (at given index).
 */
fun testOne(
    index:      Int,
    expected:   Boolean,
    classifier: Classifier,
) {
    val sample = dataset[index]
    testSame(
        classifier(sample.example),
        when (expected) {
            true  ->  sample.label
            false -> !sample.label
        },
        /* Label for this test */
        when (expected) {
            true  -> "${sample.example}"
            false -> "${sample.example} Error"
        },
    )
}

@EnabledTest
fun testNaiveClassifier() {
    
    /* Correct responses (true positives) */
    testOne( 0, true, naiveClassifier)
    testOne( 1, true, naiveClassifier)
    
    /* Incorrect responses (false negatives) */
    testOne( 2, false, naiveClassifier)
    testOne( 3, false, naiveClassifier)
    testOne( 4, false, naiveClassifier)
    testOne( 5, false, naiveClassifier)
    testOne( 6, false, naiveClassifier)
    testOne( 7, false, naiveClassifier)
    testOne( 8, false, naiveClassifier)
    
    /* Correct responses (true negatives), sometimes lucky! */
    testOne( 9, true, naiveClassifier)
    testOne(10, true, naiveClassifier)
    testOne(11, true, naiveClassifier)
    testOne(12, true, naiveClassifier)
    testOne(13, true, naiveClassifier)
    testOne(14, true, naiveClassifier)
    testOne(15, true, naiveClassifier)
    testOne(16, true, naiveClassifier)
    testOne(17, true, naiveClassifier)
}

typealias EvaluationFunction<T> = (T) -> Int

/**
 * A distance function producing a integer distance
 * between two elements of type T.
 */
typealias DistanceFunction<T> = (T, T) -> Int

data class ResultWithVotes<L>(val label: L, val votes: Int)

/**
 * Uses k-nearest-neighbor (kNN) to predict the label
 * for a supplied query given a labelled dataset and
 * and a distance function.
 */
fun <E, L> getLabel(
    query:   E,
    dataset: List<LabeledExample<E, L>>,
    distFn:  DistanceFunction<E>,
    k:       Int,
): ResultWithVotes<L> {

    /**
     * Your code.
     *
     * 1. Use top-K to find the k-closest dataset elements.
     * 
     *    Note that finding the elements whose negated distance
     *    is the greatest is the same as finding those that are
     *    closest!
     *
     * 2. Use `map { it.label }` to keep only the labels of 
     *    the k-closest elements.
     * 
     * 3. For each distinct label, count how many times it
     *    showed up in step #2.
     *
     * 4. Use topK to get the label with the greatest count.
     *
     * 5. Return both the label and the number of votes.
     */
}

@EnabledTest
fun testGetLabel() {
    /** 
     * Think of the data set below as points on a line.
     * '?' refers to the example below.
     *
     *       A   A       ?       B           B
     * |--- --- --- --- --- --- --- --- --- ---|
     *   1   2   3   4   5   6   7   8   9  10
     */
    val dataset =
        listOf(
            LabeledExample( 2, "A"),
            LabeledExample( 3, "A"),
            LabeledExample( 7, "B"),
            LabeledExample(10, "B"),
        )
    
    /**
     * A simple distance function (absolute value).
     */
    fun distance(a: Int, b: Int): Int {
        val delta = a - b
        return when (delta >= 0) {
            true  ->  delta
            false -> -delta
        }
    }

    /** 
     * Demonstrate that you understand how k-NN is
     * supposed to work by writting tests here for 
     * a selection of cases that use the data set,
     * as well as the distance function above.
     *
     * To help you get started, consider a test for
     * a point 5, with k=3:
     *
     * (a) All the points with their distances are:
     *
     *     a = | 2 - 5| = 3
     *     a = | 3 - 5| = 3
     *     b = | 7 - 5| = 2
     *     b = |10 - 5| = 5
     * 
     * (b) So, the labels of the three closest are:
     * 
     *      i) 'a' with 2 votes;
     *     ii) 'b' with 1 vote.
     *
     * (c) kNN in this situation would predict the 
     *     label for this point to be "a".
     *
     *     Its confidence is 2/3.
     *
     * We capture this test as follows:
     */
    testSame(
        getLabel(5, dataset, ::distance, k = 3),
        ResultWithVotes("a", 2),
        "5 -> A, 2/3",
    )

    /** 
     * Now your task is to write tests for the following
     * additional cases:
     *
     *  1 (k=1)
     *  1 (k=2)
     * 10 (k=1)
     * 10 (k=2)
     */
}

fun classifier(s: String): ResultWithVotes<Boolean> {
    /**
     * 1. Convert the input to lowercase (since the data
     *    set is all lowercase!)
     *
     * 2. Check to see if the lowercased input exists in
          the data set (you can assume no duplicates).

     * 3. If the input was found, simply return its label
     *    with 100% confidence (3/3).
     *    
     *    Otherwise, return the result of a 3-NN classifi-
     *    cation using the Levenshtein distance metric and
     *    the data set.
     */
}

@EnabledTest
fun testClassifier() {
    testSame(
        classifier("YES"),
        ResultWithVotes(true, 3),
        "YES: 3/3",
    )

    testSame(
        classifier("no"),
        ResultWithVotes(false, 3),
        "no: 3/3",
    )

    /* Good ML! */
    testSame(
        classifier("nadda"),
        ResultWithVotes(false, 2),
        "nadda: 2/3",
    )

    /* Good ML! */
    testSame(
        classifier("yerp"),
        ResultWithVotes(true, 3),
        "yerp: 3/3",
    )

    /* Very confident in the wrong answer. */
    testSame(
        classifier("ouch"),
        ResultWithVotes(true, 3),
        "ouch: 3/3",
    )

    /* Very confident, but does the input make sense?! */
    testSame(
        classifier("now"),
        ResultWithVotes(false, 3),
        "now 3/3",
    )
}

fun isPositive(s: String): Boolean = classifier(s).label

@EnabledTest
fun testIsPositive() {
    /* True positives (rote memorisation) */
    for (idx in 0..8) {
        helpTestElement(idx, true, ::isPositive)
    }

    /* True negatives (rote memorisation) */
    for (idx in 9..17) {
        helpTestElement(idx, true, ::isPositive)
    }
}

/*** Step 6: Putting all together ***/

/**
 * Represents the result of a study session:
 * 
 * (i)  Number of questions in the question bank; and
 * (ii) Number of attempts to get them all correct.
 */
data class StudyDeckResult(val numQuestions: Int, val numAttempts: Int)

fun main() {}

runEnabledTests(this)
main()
