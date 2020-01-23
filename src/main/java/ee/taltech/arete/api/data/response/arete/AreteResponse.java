package ee.taltech.arete.api.data.response.arete;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import ee.taltech.arete.api.data.response.hodor_studenttester.*;
import ee.taltech.arete.api.data.response.legacy.LegacyTestJobResult;
import ee.taltech.arete.api.data.response.legacy.LegacyTestingResult;
import ee.taltech.arete.domain.Submission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "response")
@Entity
@JsonClassDescription("Response sent to Moodle")
public class AreteResponse {

    @Column(length = 1023)
    String version = "arete_2.0";

    @JsonPropertyDescription("List of style, compilation and other errors")
    @OneToMany(cascade = {CascadeType.ALL})
    List<Error> errors = new ArrayList<>();

    @JsonPropertyDescription("List of student files")
    @OneToMany(cascade = {CascadeType.ALL})
    List<File> files = new ArrayList<>();

    @JsonPropertyDescription("List of test files")
    @OneToMany(cascade = {CascadeType.ALL})
    List<File> testFiles = new ArrayList<>();

    @JsonPropertyDescription("List of test suites which each contains unit-tests. Each test file produces an test suite")
    @OneToMany(cascade = {CascadeType.ALL})
    List<TestContext> testSuites = new ArrayList<>();

    @JsonPropertyDescription("Console outputs from docker")
    @OneToMany(cascade = {CascadeType.ALL})
    List<ConsoleOutput> consoleOutputs = new ArrayList<>();

    @JsonPropertyDescription("HTML result for student")
    @Column(columnDefinition = "TEXT")
    String output;

    @JsonPropertyDescription("Number of tests")
    Integer totalCount = 0;

    @Column(length = 1023)
    @JsonPropertyDescription("Passed percentage")
    String totalGrade = "0";

    @JsonPropertyDescription("Number of passed tests")
    Integer totalPassedCount = 0;

    @JsonPropertyDescription("Style percentage")
    Integer style = 100;

    @Column(length = 1023)
    @JsonPropertyDescription("Slug ran for student. for example pr01_something")
    String slug;

    @Transient
    @JsonPropertyDescription("values that are returned the same way they were given in")
    @JsonProperty("returnExtra")
    JsonNode returnExtra;

    @JsonPropertyDescription("Commit hash from gitlab")
    String hash;

    @JsonPropertyDescription("Students uniid")
    String uniid;

    @JsonPropertyDescription("Testing timestamp")
    Long timestamp;

    @JsonPropertyDescription("Commit message for student repository")
    String commitMessage;

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    public AreteResponse(String slug, Submission submission, String message) { //Failed submission
        Error error = new Error.ErrorBuilder().columnNo(0).lineNo(0).fileName("tester").message(message).build();
        this.hash = submission.getHash();
        this.uniid = submission.getUniid();
        this.timestamp = submission.getTimestamp();
        this.commitMessage = submission.getCommitMessage();
        this.output = message;
        this.errors.add(error);

        if (submission.getSystemExtra() != null && !submission.getSystemExtra().contains("noStd")) {
            consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
        }

        if (submission.getResponse() == null) {
            submission.setResponse(new ArrayList<>());
        }

        submission.getResponse().add(this);
        this.returnExtra = submission.getReturnExtra();
        this.slug = slug;
    }

    public AreteResponse(String slug, Submission submission, hodorStudentTesterResponse response) { //Successful submission

        for (TestingResult result : response.getResults()) {

            if (result.getTotalCount() != null) {
                totalCount = result.getTotalCount();
            }

            if (result.getTotalGrade() != null) {
                totalGrade = result.getTotalGrade();
            }

            if (result.getTotalPassedCount() != null) {
                totalPassedCount = result.getTotalPassedCount();
            }

            if (result.getErrors() != null) {
                for (StyleError warning : result.getErrors()) {
                    Error areteWarning = new Error.ErrorBuilder()
                            .columnNo(warning.getColumnNo())
                            .lineNo(warning.getLineNo())
                            .fileName(warning.getFileName())
                            .message(warning.getMessage())
                            .kind("style error")
                            .build();
                    errors.add(areteWarning);
                    style = 0;
                }
            }

            if (result.getFiles() != null) {
                for (HodorFile file : result.getFiles()) {
                    File areteFile = new File.FileBuilder()
                            .path(file.getPath())
                            .contents(file.getContents())
                            .build();
                    if (!submission.getSystemExtra().contains("noFiles")) {
                        if (file.getIsTest()) {
                            if (!submission.getSystemExtra().contains("noTesterFiles")) {
                                testFiles.add(areteFile);
                            }
                        } else {
                            if (!submission.getSystemExtra().contains("noStudentFiles")) {
                                testFiles.add(areteFile);
                            }
                        }
                    }
                }
            }

            if (result.getDiagnosticList() != null) {
                for (Diagnostic warning : result.getDiagnosticList()) {
                    Error areteWarning = new Error.ErrorBuilder()
                            .columnNo(warning.getColumnNo())
                            .lineNo(warning.getLineNo())
                            .fileName(warning.getFile())
                            .message(warning.getMessage())
                            .hint(warning.getHint())
                            .kind(warning.getKind() == null ? "Diagnostic error" : warning.getKind())
                            .build();
                    errors.add(areteWarning);
                    style = 0;
                }
            }

            if (result.getTestContexts() != null) {
                testSuites.addAll(result.getTestContexts());
            }

        }

        output = constructOutput(submission);
        fillFromSubmission(slug, submission);
    }

    public AreteResponse(String slug, Submission submission, LegacyTestJobResult response) {

        for (LegacyTestingResult result : response.getResults()) {
            TestContext textContext = new TestContext();
            textContext.name = result.getName();
            textContext.weight = 1;
            textContext.grade = result.getPercentage();
            this.totalGrade = String.valueOf(result.getPercentage());
            this.testSuites.add(textContext);
        }
        this.output = response.getOutput();

        if (!submission.getSystemExtra().contains("noFiles")) {
            this.files = response.getFiles();
        }

        if (response.getPercent() != null) {
            this.totalGrade = String.valueOf(response.getPercent());
        }

        fillFromSubmission(slug, submission);
    }

    private static void tr(StringBuilder output) {
        output.append("<tr style='border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
    }

    private static void td(StringBuilder output) {
        output.append("<td style='color:#D5DDE5;background:#4E5066;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
    }

    private static void td(StringBuilder output, String extra) {
        output.append("<td style='color:#D5DDE5;background:#4E5066;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;' ").append(extra).append(">");
    }

    private static void th(StringBuilder output) {
        output.append("<th style='color:#D5DDE5;background:#1b1e24;border: 1px solid black;border-collapse: collapse;padding: 5px;text-align: left;'>");
    }

    private static String getRandomQuote() {
        String[] quotes = new String[]{"Random quote by Kevin Kruse: Life isn’t about getting and having, it’s about giving and being.", "Random quote by Napoleon Hill: Whatever the mind of man can conceive and believe, it can achieve.", "Random quote by Albert Einstein: Strive not to be a success, but rather to be of value.", "Random quote by Robert Frost: Two roads diverged in a wood, and I—I took the one less traveled by, And that has made all the difference.", "Random quote by Florence Nightingale: I attribute my success to this: I never gave or took any excuse.", "Random quote by Wayne Gretzky: You miss 100% of the shots you don’t take.", "Random quote by Michael Jordan: I’ve missed more than 9000 shots in my career. I’ve lost almost 300 games. 26 times I’ve been trusted to take the game winning shot and missed. I’ve failed over and over and over again in my life. And that is why I succeed.", "Random quote by Amelia Earhart: The most difficult thing is the decision to act, the rest is merely tenacity.", "Random quote by Babe Ruth: Every strike brings me closer to the next home run.", "Random quote by W. Clement Stone: Definiteness of purpose is the starting point of all achievement.", "Random quote by Kevin Kruse: We must balance conspicuous consumption with conscious capitalism.", "Random quote by John Lennon: Life is what happens to you while you’re busy making other plans.", "Random quote by Earl Nightingale: We become what we think about.", "Random quote by Mark Twain: 14.Twenty years from now you will be more disappointed by the things that you didn’t do than by the ones you did do, so throw off the bowlines, sail away from safe harbor, catch the trade winds in your sails.  Explore, Dream, Discover.", "Random quote by Charles Swindoll: 15.Life is 10% what happens to me and 90% of how I react to it.", "Random quote by Alice Walker: The most common way people give up their power is by thinking they don’t have any.", "Random quote by Buddha: The mind is everything. What you think you become.", "Random quote by Chinese Proverb: The best time to plant a tree was 20 years ago. The second best time is now.", "Random quote by Socrates: An unexamined life is not worth living.", "Random quote by Woody Allen: Eighty percent of success is showing up.", "Random quote by Steve Jobs: Your time is limited, so don’t waste it living someone else’s life.", "Random quote by Vince Lombardi: Winning isn’t everything, but wanting to win is.", "Random quote by Stephen Covey: I am not a product of my circumstances. I am a product of my decisions.", "Random quote by Pablo Picasso: Every child is an artist.  The problem is how to remain an artist once he grows up.", "Random quote by Christopher Columbus: You can never cross the ocean until you have the courage to lose sight of the shore.", "Random quote by Maya Angelou: I’ve learned that people will forget what you said, people will forget what you did, but people will never forget how you made them feel.", "Random quote by Jim Rohn: Either you run the day, or the day runs you.", "Random quote by Henry Ford: Whether you think you can or you think you can’t, you’re right.", "Random quote by Mark Twain: The two most important days in your life are the day you are born and the day you find out why.", "Random quote by Johann Wolfgang von Goethe: Whatever you can do, or dream you can, begin it.  Boldness has genius, power and magic in it.", "Random quote by Frank Sinatra: The best revenge is massive success.", "Random quote by Zig Ziglar: People often say that motivation doesn’t last. Well, neither does bathing.  That’s why we recommend it daily.", "Random quote by Anais Nin: Life shrinks or expands in proportion to one’s courage.", "Random quote by Vincent Van Gogh: If you hear a voice within you say “you cannot paint,” then by all means paint and that voice will be silenced.", "Random quote by Aristotle: There is only one way to avoid criticism: do nothing, say nothing, and be nothing.", "Random quote by Jesus: Ask and it will be given to you; search, and you will find; knock and the door will be opened for you.", "Random quote by Ralph Waldo Emerson: The only person you are destined to become is the person you decide to be.", "Random quote by Henry David Thoreau: Go confidently in the direction of your dreams.  Live the life you have imagined.", "Random quote by Erma Bombeck: When I stand before God at the end of my life, I would hope that I would not have a single bit of talent left and could say, I used everything you gave me.", "Random quote by Booker T. Washington: Few things can help an individual more than to place responsibility on him, and to let him know that you trust him.", "Random quote by  Ancient Indian Proverb: Certain things catch your eye, but pursue only those that capture the heart.", "Random quote by Theodore Roosevelt: Believe you can and you’re halfway there.", "Random quote by George Addair: Everything you’ve ever wanted is on the other side of fear.", "Random quote by Plato: We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.", "Random quote by Maimonides: Teach thy tongue to say, “I do not know,” and thous shalt progress.", "Random quote by Arthur Ashe: Start where you are. Use what you have.  Do what you can.", "Random quote by John Lennon: When I was 5 years old, my mother always told me that happiness was the key to life.  When I went to school, they asked me what I wanted to be when I grew up.  I wrote down ‘happy’.  They told me I didn’t understand the assignment, and I told them they didn’t understand life.", "Random quote by Japanese Proverb: Fall seven times and stand up eight.", "Random quote by Helen Keller: When one door of happiness closes, another opens, but often we look so long at the closed door that we do not see the one that has been opened for us.", "Random quote by Confucius: Everything has beauty, but not everyone can see.", "Random quote by Anne Frank: How wonderful it is that nobody need wait a single moment before starting to improve the world.", "Random quote by Lao Tzu: When I let go of what I am, I become what I might be.", "Random quote by Maya Angelou: Life is not measured by the number of breaths we take, but by the moments that take our breath away.", "Random quote by Dalai Lama: Happiness is not something readymade.  It comes from your own actions.", "Random quote by Sheryl Sandberg: If you’re offered a seat on a rocket ship, don’t ask what seat! Just get on.", "Random quote by Aristotle: First, have a definite, clear practical ideal; a goal, an objective. Second, have the necessary means to achieve your ends; wisdom, money, materials, and methods. Third, adjust all your means to that end.", "Random quote by Latin Proverb: If the wind will not serve, take to the oars.", "Random quote by Unknown: You can’t fall if you don’t climb.  But there’s no joy in living your whole life on the ground.", "Random quote by Marie Curie: We must believe that we are gifted for something, and that this thing, at whatever cost, must be attained.", "Random quote by Les Brown: Too many of us are not living our dreams because we are living our fears.", "Random quote by Joshua J. Marine: Challenges are what make life interesting and overcoming them is what makes life meaningful.", "Random quote by Booker T. Washington: If you want to lift yourself up, lift up someone else.", "Random quote by Leonardo da Vinci: I have been impressed with the urgency of doing. Knowing is not enough; we must apply. Being willing is not enough; we must do.", "Random quote by Jamie Paolinetti: Limitations live only in our minds.  But if we use our imaginations, our possibilities become limitless.", "Random quote by Erica Jong: You take your life in your own hands, and what happens? A terrible thing, no one to blame.", "Random quote by Bob Dylan: What’s money? A man is a success if he gets up in the morning and goes to bed at night and in between does what he wants to do.", "Random quote by Benjamin Franklin: I didn’t fail the test. I just found 100 ways to do it wrong.", "Random quote by Bill Cosby: In order to succeed, your desire for success should be greater than your fear of failure.", "Random quote by  Albert Einstein: A person who never made a mistake never tried anything new.", "Random quote by Chinese Proverb: The person who says it cannot be done should not interrupt the person who is doing it.", "Random quote by Roger Staubach: There are no traffic jams along the extra mile.", "Random quote by George Eliot: It is never too late to be what you might have been.", "Random quote by Oprah Winfrey: You become what you believe.", "Random quote by Vincent van Gogh: I would rather die of passion than of boredom.", "Random quote by Unknown: A truly rich man is one whose children run into his arms when his hands are empty.", "Random quote by Ann Landers: It is not what you do for your children, but what you have taught them to do for themselves, that will make them successful human beings.", "Random quote by Abigail Van Buren: If you want your children to turn out well, spend twice as much time with them, and half as much money.", "Random quote by Farrah Gray: Build your own dreams, or someone else will hire you to build theirs.", "Random quote by Jesse Owens: The battles that count aren’t the ones for gold medals. The struggles within yourself–the invisible battles inside all of us–that’s where it’s at.", "Random quote by Sir Claus Moser: Education costs money.  But then so does ignorance.", "Random quote by Rosa Parks: I have learned over the years that when one’s mind is made up, this diminishes fear.", "Random quote by Confucius: It does not matter how slowly you go as long as you do not stop.", "Random quote by Oprah Winfrey: If you look at what you have in life, you’ll always have more. If you look at what you don’t have in life, you’ll never have enough.", "Random quote by Dalai Lama: Remember that not getting what you want is sometimes a wonderful stroke of luck.", "Random quote by Maya Angelou: You can’t use up creativity.  The more you use, the more you have.", "Random quote by Norman Vaughan: Dream big and dare to fail.", "Random quote by Martin Luther King Jr.: Our lives begin to end the day we become silent about things that matter.", "Random quote by Teddy Roosevelt: Do what you can, where you are, with what you have.", "Random quote by Tony Robbins: If you do what you’ve always done, you’ll get what you’ve always gotten.", "Random quote by Gloria Steinem: Dreaming, after all, is a form of planning.", "Random quote by Mae Jemison: It’s your place in the world; it’s your life. Go on and do all you can with it, and make it the life you want to live.", "Random quote by Beverly Sills: You may be disappointed if you fail, but you are doomed if you don’t try.", "Random quote by Eleanor Roosevelt: Remember no one can make you feel inferior without your consent.", "Random quote by Grandma Moses: Life is what we make it, always has been, always will be.", "Random quote by Ayn Rand: The question isn’t who is going to let me; it’s who is going to stop me.", "Random quote by Henry Ford: When everything seems to be going against you, remember that the airplane takes off against the wind, not with it.", "Random quote by Abraham Lincoln: It’s not the years in your life that count. It’s the life in your years.", "Random quote by Norman Vincent Peale: Change your thoughts and you change your world.", "Random quote by Benjamin Franklin: Either write something worth reading or do something worth writing.", "Random quote by –Audrey Hepburn: Nothing is impossible, the word itself says, “I’m possible!”", "Random quote by Steve Jobs: The only way to do great work is to love what you do.", "Random quote by Zig Ziglar: If you can dream it, you can achieve it."};
        Random random = new Random();
        return quotes[random.nextInt(quotes.length)];
    }

    private void fillFromSubmission(String slug, Submission submission) {

        consoleOutputs.add(new ConsoleOutput.ConsoleOutputBuilder().content(submission.getResult()).build());
        if (submission.getResponse() == null) {
            submission.setResponse(new ArrayList<>());
        }
        submission.getResponse().add(this);
        this.returnExtra = submission.getReturnExtra();
        this.slug = slug;
        this.hash = submission.getHash();
        this.uniid = submission.getUniid();
        this.timestamp = submission.getTimestamp();
        this.commitMessage = submission.getCommitMessage();
    }

    private String constructOutput(Submission submission) {
        StringBuilder output = new StringBuilder();

        if (submission.getUniid() == null) {
            output.append(String.format("<h2>Testing results for %s</h2>", submission.getUniid()));
        } else {
            output.append("<h2>Testing results</h2>");
        }
        output.append(String.format("<p>Submission hash: %s</p>", submission.getHash()));
        if (!submission.getSystemExtra().contains("noFun")) {
            output.append(String.format("<br>%s<br>", getRandomQuote()));
        }

        long totalSize = 0;
        long totalPassed = 0;

        long totalWeight = 0;
        long totalPassedWeight = 0;

        errorTable(output);


        for (TestContext context : testSuites) {

            if (context.unitTests.size() != 0) {

                output.append("<br>");
                testsTable(submission, output, context);

                List<String> positive = Arrays.asList("success", "passed");

                long size = context.unitTests.size();
                totalSize += size;
                output.append(String.format("<p>Number of tests: %s</p>", size));

                long passed = context.unitTests.stream().filter(x -> positive.contains(x.status.toLowerCase())).count();
                totalPassed += passed;
                output.append(String.format("<p>Passed tests: %s</p>", passed));

                long weights = context.unitTests.stream()
                        .map(x -> x.weight)
                        .mapToInt(Integer::intValue)
                        .sum();
                totalWeight += weights;
                output.append(String.format("<p>Total weight: %s</p>", weights));

                long passedWeights = context.unitTests.stream()
                        .filter(x -> positive.contains(x.status.toLowerCase()))
                        .map(x -> x.weight)
                        .mapToInt(Integer::intValue)
                        .sum();
                totalPassedWeight += passedWeights;
                output.append(String.format("<p>Passed weight: %s</p>", passedWeights));

                String totalGrade = String.format("%s", Math.round((float) passedWeights / (float) weights * 100 * 100.0) / 100.0);
                output.append(String.format("<p>Percentage: %s%s</p>", totalGrade, "%"));
            }
        }

        this.totalCount = Math.toIntExact(totalSize);
        this.totalPassedCount = Math.toIntExact(totalPassed);

        output.append("<br>");
        output.append("<br>");
        output.append("<h2>Overall</h2>");

        output.append(String.format("<p>Total number of tests: %s</p>", totalSize));
        output.append(String.format("<p>Total passed tests: %s</p>", totalPassed));
        output.append(String.format("<p>Total weight: %s</p>", totalWeight));
        output.append(String.format("<p>Total Passed weight: %s</p>", totalPassedWeight));

        String totalGrade = String.format("%s", Math.round((float) totalPassedWeight / (float) totalWeight * 100 * 100.0) / 100.0);
        this.totalGrade = totalGrade;
        output.append(String.format("<p>Total Percentage: %s%s</p>", totalGrade, "%"));

        output.append("<br>");
        output.append("<br>");

        Date date = new Date(submission.getTimestamp());
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Estonia/Tallinn"));
        String formatted = format.format(date);
        output.append(String.format("<p>Timestamp: %s</p>", formatted));
        return output.toString();
    }

    private void TestsHeader(StringBuilder output, String headerName) {
        th(output);
        output.append(headerName);
        output.append("</th>");

        th(output);
        output.append("Result");
        output.append("</th>");

        th(output);
        output.append("Time (ms)");
        output.append("</th>");

        th(output);
        output.append("Weight");
        output.append("</th>");
    }

    private void errorTable(StringBuilder output) {
        output.append("<br>");
        output.append("<table style='width:100%;border: 1px solid black;border-collapse: collapse;' id='errors'>");

        tr(output);

        th(output);
        output.append("File");
        output.append("</th>");

        th(output);
        output.append("Line");
        output.append("</th>");

        th(output);
        output.append("Column");
        output.append("</th>");

        th(output);
        output.append("Error");
        output.append("</th>");

        output.append("</tr>");

        for (Error error : errors) {

            tr(output);
            String[] name;
            if (error.fileName != null) {
                name = error.fileName.split("/");
            } else {
                name = new String[]{"null"};
            }

            td(output);
            output.append(name[name.length - 1]);
            output.append("</td>");

            td(output);
            output.append(error.lineNo);
            output.append("</td>");

            td(output);
            output.append(error.columnNo);
            output.append("</td>");

            td(output);
            output.append(error.message);
            output.append("</td>");


            output.append("</tr>");

            if (error.hint != null && !error.hint.equals("")) {
                tr(output);

                td(output);
                output.append("Hint");
                output.append("</td>");

                td(output, "colspan='3'");
                output.append(error.hint.replace("\n", ""));
                output.append("</td>");
                output.append("</tr>");
            }
        }

        output.append("</table>");

        output.append("<br>");
        output.append(String.format("Style percentage: %s%s", style, "%"));
        output.append("<br>");
    }

    private void testsTable(Submission submission, StringBuilder output, TestContext context) {
        output.append("<br>");
        output.append("<table style='width:100%;border: 1px solid black;border-collapse: collapse;'>");

        TestsHeader(output, context.file);

        context.unitTests.sort((o1, o2) -> {
            if (o1.status.equals(o2.status)) {
                return 0;
            }

            List<String> results = Arrays.asList("success", "partial_success", "passed", "skipped", "not_run", "failure", "failed", "not_set", "unknown");

            int place1 = results.indexOf(o1.status.toLowerCase());
            int place2 = results.indexOf(o2.status.toLowerCase());
            return place1 < place2 ? -1 : 1;
        });

        for (UnitTest test : context.unitTests) {

            tr(output);
            td(output);
            output.append(test.name);

            if (!submission.getSystemExtra().contains("noFeedback") && test.getPrintExceptionMessage() != null && test.getPrintExceptionMessage() && test.getExceptionMessage() != null) {

                output.append(String.format("<br><a style='color:red;'>%s</a>: ", test.getExceptionClass().equals("") ? "Exception" : test.getExceptionClass()));
                if (test.getExceptionMessage() == null || test.getExceptionMessage().equals("") || test.getExceptionClass().toLowerCase().contains("assert") && submission.getSystemExtra().contains("minimalFeedback")) {
                    output.append(" ... ");
                } else {
                    output.append(test.getExceptionMessage());
                }
            }

            if (!submission.getSystemExtra().contains("noFeedback") && test.getPrintStackTrace() != null && test.getPrintStackTrace() && test.getStackTrace() != null) {

                output.append(String.format("<br>%s:%s", "Stacktrace", test.getStackTrace()));

            }

            output.append("</td>");

            td(output);

            List<String> GREEN = Arrays.asList("success", "passed");
            List<String> YELLOW = Arrays.asList("partial_success", "skipped");
            List<String> RED = Arrays.asList("not_run", "failure", "failed", "not_set", "unknown");

            if (GREEN.contains(test.status.toLowerCase())) {
                output.append(String.format("<p style='color:greenyellow;'>%s</p>", test.status));
            } else if (YELLOW.contains(test.status.toLowerCase())) {
                output.append(String.format("<p style='color:yellow;'>%s</p>", test.status));
            } else if (RED.contains(test.status.toLowerCase())) {
                output.append(String.format("<p style='color:red;'>%s</p>", test.status));
            }

            output.append("</td>");

            td(output);
            output.append(test.timeElapsed);
            output.append("</td>");

            td(output);
            output.append(test.weight);
            output.append("</td>");

            output.append("</tr>");

        }
        output.append("</table>");
    }

}
