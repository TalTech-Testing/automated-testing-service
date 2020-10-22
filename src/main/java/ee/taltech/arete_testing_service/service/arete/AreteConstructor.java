package ee.taltech.arete_testing_service.service.arete;

import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete.java.TestStatus;
import ee.taltech.arete.java.response.arete.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AreteConstructor {

    public static AreteResponseDTO failedSubmission(String slug, Submission submission, String cause) {
        String message = "Testing slug: " + slug + " failed with a message: " + cause;
        ErrorDTO error = ErrorDTO.builder().columnNo(0).lineNo(0).fileName("tester").message(message).build();

        AreteResponseDTO response = AreteResponseDTO.builder()
                .failed(true)
                .output(message)
                .errors(List.of(error))
                .build();

        AreteConstructor.fillFromSubmission(slug, submission, response);

        return response;
    }

    public static void fillFromSubmission(String slug, Submission submission, AreteResponseDTO response) {
        response.getConsoleOutputs().add(new ConsoleOutputDTO(submission.getResult()));
        response.setReturnExtra(submission.getReturnExtra());
        response.setSlug(slug);
        response.setHash(submission.getHash());
        response.setUniid(submission.getUniid());
        response.setTimestamp(submission.getTimestamp());
        response.setReceivedTimestamp(submission.getReceivedTimestamp());
        response.setFinishedTimestamp(System.currentTimeMillis());
        response.setCommitMessage(submission.getCommitMessage());
        response.setTestingPlatform(submission.getTestingPlatform());
        response.setRoot(submission.getCourse());
        response.setGitStudentRepo(submission.getGitStudentRepo());
        response.setGitTestRepo(submission.getGitTestRepo());
        response.setPriority(submission.getPriority());
        response.setDockerTimeout(submission.getDockerTimeout());
        response.setEmail(submission.getEmail());

        if (response.getDockerExtra() != null) {
            submission.getDockerExtra().addAll(response.getDockerExtra());
        }

        if (response.getSystemExtra() != null) {
            submission.getSystemExtra().addAll(response.getSystemExtra());
        }

        response.setDockerExtra(submission.getDockerExtra());
        response.setSystemExtra(submission.getSystemExtra());

        if (response.getFiles() == null || response.getFiles().size() == 0) {
            response.setFiles(submission.getSource());
        }
        if (response.getTestFiles() == null || response.getTestFiles().size() == 0) {
            response.setTestFiles(submission.getTestSource());
        }

        dropConfiguredFields(submission, response);
        constructOutput(submission, response);
    }

    private static void dropConfiguredFields(Submission submission, AreteResponseDTO response) {
        if (submission.getSystemExtra().contains("noFiles")) {
            response.setTestFiles(new ArrayList<>());
            response.setFiles(new ArrayList<>());
        }

        if (submission.getSystemExtra().contains("noTesterFiles")) {
            response.setTestFiles(new ArrayList<>());
        }

        if (submission.getSystemExtra().contains("noStudentFiles")) {
            response.setFiles(new ArrayList<>());
        }

        if (submission.getSystemExtra().contains("noStd")) {
            response.setConsoleOutputs(new ArrayList<>());
            response.getTestSuites().forEach(x -> x.getUnitTests().forEach(y -> y.setStderr(new ArrayList<>())));
            response.getTestSuites().forEach(x -> x.getUnitTests().forEach(y -> y.setStdout(new ArrayList<>())));
        }
    }

    private static void constructOutput(Submission submission, AreteResponseDTO response) {
        StringBuilder output = new StringBuilder();

        if (submission.getUniid() != null) {
            output.append(String.format("<h2>Testing results for %s</h2>", submission.getUniid()));
        } else {
            output.append("<h2>Testing results</h2>");
        }
        output.append(String.format("<p>Submission hash: %s</p>", submission.getHash()));
        if (!submission.getSystemExtra().contains("noFun")) {
            output.append(String.format("<br><p>%s</p><br><br>", getRandomQuote()));
        }

        long totalSize = 0;
        long totalPassed = 0;

        long totalWeight = 0;
        long totalPassedWeight = 0;

        if (submission.getSystemExtra().contains("noStyle")) {
            response.setErrors(response.getErrors().stream()
                    .filter(x -> !x.getKind().toLowerCase().contains("style")).collect(Collectors.toList()));
        }

        if (response.getErrors().size() > 0) {
            errorTable(output, response);
        }

        if (!submission.getSystemExtra().contains("noStyle")) {
            output.append("<br>");
            output.append(String.format("Style percentage: %s%s", response.getStyle(), "%"));
            output.append("<br>");
        }

        for (TestContextDTO context : response.getTestSuites()) {

            if (context.getUnitTests().size() != 0) {

                output.append("<br>");
                long size = context.getUnitTests().size();
                totalSize += size;

                long passed = context.getUnitTests().stream().filter(x -> x.getStatus().equals(TestStatus.PASSED)).count();
                totalPassed += passed;

                long weights = context.getUnitTests().stream()
                        .map(UnitTestDTO::getWeight)
                        .mapToInt(Integer::intValue)
                        .sum();
                totalWeight += weights;

                testsTable(submission, output, context, size == passed);

                output.append(String.format("<p>Number of tests: %s</p>", size));

                output.append(String.format("<p>Passed tests: %s</p>", passed));

                output.append(String.format("<p>Total weight: %s</p>", weights));

                long passedWeights = context.getUnitTests().stream()
                        .filter(x -> x.getStatus().equals(TestStatus.PASSED))
                        .map(UnitTestDTO::getWeight)
                        .mapToInt(Integer::intValue)
                        .sum();
                totalPassedWeight += passedWeights;
                output.append(String.format("<p>Passed weight: %s</p>", passedWeights));

                String totalGrade = String.format("%s", Math.round((float) passedWeights / (float) weights * 100 * 100.0) / 100.0);
                output.append(String.format("<p>Percentage: %s%s</p>", totalGrade, "%"));
            }
        }

        response.setTotalCount(Math.toIntExact(totalSize));
        response.setTotalPassedCount(Math.toIntExact(totalPassed));

        Double totalGrade = Math.round((double) totalPassedWeight / (double) totalWeight * 100 * 100.0) / 100.0;
        response.setTotalGrade(totalGrade);

        if (!submission.getSystemExtra().contains("noOverall")) {
            output.append("<br>");
            output.append("<br>");
            output.append("<h2>Overall</h2>");

            output.append(String.format("<p>Total number of tests: %s</p>", totalSize));
            output.append(String.format("<p>Total passed tests: %s</p>", totalPassed));
            output.append(String.format("<p>Total weight: %s</p>", totalWeight));
            output.append(String.format("<p>Total Passed weight: %s</p>", totalPassedWeight));
            output.append(String.format("<p>Total Percentage: %s%s</p>", totalGrade, "%"));
        }

        output.append("<br>");
        output.append("<br>");

        Date date = new Date(submission.getTimestamp());
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Europe/Tallinn"));
        String formatted = format.format(date);
        output.append(String.format("<p>Timestamp: %s</p>", formatted));
        response.setOutput(output.toString());
    }

    private static void errorTable(StringBuilder output, AreteResponseDTO response) {
        output.append("<br>");
        output.append("<table style='width:100%;border: 1px solid black;border-collapse: collapse;' id='errors'>");

        TableGenerator.tr(output);

        TableGenerator.th(output);
        output.append("File");
        output.append("</th>");

        TableGenerator.th(output);
        output.append("Line");
        output.append("</th>");

        TableGenerator.th(output);
        output.append("Column");
        output.append("</th>");

        TableGenerator.th(output);
        output.append("Error");
        output.append("</th>");

        output.append("</tr>");

        for (ErrorDTO error : response.getErrors()) {

            TableGenerator.tr(output);
            String[] name;
            if (error.getFileName() != null) {
                name = error.getFileName().split("/");
            } else {
                name = new String[]{"null"};
            }

            String hex;
            if (error.getKind() == null || error.getKind().toLowerCase().contains("style") || error.getKind().toLowerCase().contains("diagnostic")) {
                hex = "393939";
            } else {
                hex = "8b0000";
            }

            TableGenerator.td_hex(output, hex);
            output.append(name[name.length - 1]);
            output.append("</td>");

            TableGenerator.td_hex(output, hex);
            output.append(error.getLineNo());
            output.append("</td>");

            TableGenerator.td_hex(output, hex);
            output.append(error.getColumnNo());
            output.append("</td>");

            TableGenerator.td_hex(output, hex);
            output.append(error.getMessage());
            output.append("</td>");

            output.append("</tr>");

            if (error.getHint() != null && !error.getHint().equals("")) {
                TableGenerator.tr(output);

                TableGenerator.td(output);
                output.append("Hint");
                output.append("</td>");

                TableGenerator.td_extra(output, "colspan='3'");
                output.append(error.getHint().replace("\n", ""));
                output.append("</td>");
                output.append("</tr>");
            }
        }

        output.append("</table>");
    }

    private static void testsTable(Submission submission, StringBuilder output, TestContextDTO context, boolean bright) {
        output.append("<br>");
        output.append("<table style='width:100%;border: 1px solid black;border-collapse: collapse;'>");

        String filename = context.getFile();
        if (filename == null) {
            filename = "test_file";
        }
        String[] file = filename.split("[/\\\\]");
        TableGenerator.TestsHeader(output, file[file.length - 1]);

        context.getUnitTests().sort((o1, o2) -> {
            if (o1.getStatus().equals(o2.getStatus())) {
                return 0;
            }

            List<TestStatus> results = Arrays.asList(TestStatus.PASSED, TestStatus.SKIPPED, TestStatus.FAILED);

            int place1 = results.indexOf(o1.getStatus());
            int place2 = results.indexOf(o2.getStatus());
            return place1 < place2 ? -1 : 1;
        });

        for (UnitTestDTO test : context.getUnitTests()) {

            TableGenerator.tr(output);
            TableGenerator.td(output, bright);

            if (bright) {
                output.append(String.format("<p style='color:black;'>%s</p>", test.getName()));
            } else {
                output.append(test.getName());
            }

            if (!submission.getSystemExtra().contains("noFeedback") && test.getPrintExceptionMessage() != null && test.getPrintExceptionMessage() && test.getExceptionClass() != null && !test.getExceptionClass().equals("")) {

                output.append(String.format("<br><a style='color:red;'>%s</a>: ", test.getExceptionClass()));

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

            TableGenerator.td(output, bright);

            if (test.getStatus().equals(TestStatus.PASSED)) {
                output.append(String.format("<p style='color:greenyellow;'>%s</p>", test.getStatus()));
            } else if (test.getStatus().equals(TestStatus.SKIPPED)) {
                output.append(String.format("<p style='color:yellow;'>%s</p>", test.getStatus()));
            } else if (test.getStatus().equals(TestStatus.FAILED)) {
                output.append(String.format("<p style='color:red;'>%s</p>", test.getStatus()));
            }

            output.append("</td>");

            TableGenerator.td(output, bright);
            if (bright) {
                output.append(String.format("<p style='color:black;'>%s</p>", test.getTimeElapsed()));
            } else {
                output.append(test.getTimeElapsed());
            }
            output.append("</td>");

            TableGenerator.td(output, bright);
            if (bright) {
                output.append(String.format("<p style='color:black;'>%s</p>", test.getWeight()));
            } else {
                output.append(test.getWeight());
            }
            output.append("</td>");

            output.append("</tr>");

        }
        output.append("</table>");
    }

    private static String getRandomQuote() {
        String[] quotes = new String[]{"Quote by Kevin Kruse: \"Life isn’t about getting and having, it’s about giving and being.\"", "Quote by Napoleon Hill: \"Whatever the mind of man can conceive and believe, it can achieve.\"", "Quote by Albert Einstein: \"Strive not to be a success, but rather to be of value.\"", "Quote by Robert Frost: \"Two roads diverged in a wood, and I—I took the one less traveled by, And that has made all the difference.\"", "Quote by Florence Nightingale: \"I attribute my success to this: I never gave or took any excuse.\"", "Quote by Wayne Gretzky: \"You miss 100% of the shots you don’t take.\"", "Quote by Michael Jordan: \"I’ve missed more than 9000 shots in my career. I’ve lost almost 300 games. 26 times I’ve been trusted to take the game winning shot and missed. I’ve failed over and over and over again in my life. And that is why I succeed.\"", "Quote by Amelia Earhart: \"The most difficult thing is the decision to act, the rest is merely tenacity.\"", "Quote by Babe Ruth: \"Every strike brings me closer to the next home run.\"", "Quote by W. Clement Stone: \"Definiteness of purpose is the starting point of all achievement.\"", "Quote by Kevin Kruse: \"We must balance conspicuous consumption with conscious capitalism.\"", "Quote by John Lennon: \"Life is what happens to you while you’re busy making other plans.\"", "Quote by Earl Nightingale: \"We become what we think about.\"", "Quote by Mark Twain: \"14.Twenty years from now you will be more disappointed by the things that you didn’t do than by the ones you did do, so throw off the bowlines, sail away from safe harbor, catch the trade winds in your sails.  Explore, Dream, Discover.\"", "Quote by Charles Swindoll: \"15.Life is 10% what happens to me and 90% of how I react to it.\"", "Quote by Alice Walker: \"The most common way people give up their power is by thinking they don’t have any.\"", "Quote by Buddha: \"The mind is everything. What you think you become.\"", "Quote by Chinese Proverb: \"The best time to plant a tree was 20 years ago. The second best time is now.\"", "Quote by Socrates: \"An unexamined life is not worth living.\"", "Quote by Woody Allen: \"Eighty percent of success is showing up.\"", "Quote by Steve Jobs: \"Your time is limited, so don’t waste it living someone else’s life.\"", "Quote by Vince Lombardi: \"Winning isn’t everything, but wanting to win is.\"", "Quote by Stephen Covey: \"I am not a product of my circumstances. I am a product of my decisions.\"", "Quote by Pablo Picasso: \"Every child is an artist.  The problem is how to remain an artist once he grows up.\"", "Quote by Christopher Columbus: \"You can never cross the ocean until you have the courage to lose sight of the shore.\"", "Quote by Maya Angelou: \"I’ve learned that people will forget what you said, people will forget what you did, but people will never forget how you made them feel.\"", "Quote by Jim Rohn: \"Either you run the day, or the day runs you.\"", "Quote by Henry Ford: \"Whether you think you can or you think you can’t, you’re right.\"", "Quote by Mark Twain: \"The two most important days in your life are the day you are born and the day you find out why.\"", "Quote by Johann Wolfgang von Goethe: \"Whatever you can do, or dream you can, begin it.  Boldness has genius, power and magic in it.\"", "Quote by Frank Sinatra: \"The best revenge is massive success.\"", "Quote by Zig Ziglar: \"People often say that motivation doesn’t last. Well, neither does bathing.  That’s why we recommend it daily.\"", "Quote by Anais Nin: \"Life shrinks or expands in proportion to one’s courage.\"", "Quote by Vincent Van Gogh: \"If you hear a voice within you say “you cannot paint,” then by all means paint and that voice will be silenced.\"", "Quote by Aristotle: \"There is only one way to avoid criticism: do nothing, say nothing, and be nothing.\"", "Quote by Jesus: \"Ask and it will be given to you; search, and you will find; knock and the door will be opened for you.\"", "Quote by Ralph Waldo Emerson: \"The only person you are destined to become is the person you decide to be.\"", "Quote by Henry David Thoreau: \"Go confidently in the direction of your dreams.  Live the life you have imagined.\"", "Quote by Erma Bombeck: \"When I stand before God at the end of my life, I would hope that I would not have a single bit of talent left and could say, I used everything you gave me.\"", "Quote by Booker T. Washington: \"Few things can help an individual more than to place responsibility on him, and to let him know that you trust him.\"", "Quote by  Ancient Indian Proverb: \"Certain things catch your eye, but pursue only those that capture the heart.\"", "Quote by Theodore Roosevelt: \"Believe you can and you’re halfway there.\"", "Quote by George Addair: \"Everything you’ve ever wanted is on the other side of fear.\"", "Quote by Plato: \"We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.\"", "Quote by Maimonides: \"Teach thy tongue to say, “I do not know,” and thous shalt progress.\"", "Quote by Arthur Ashe: \"Start where you are. Use what you have.  Do what you can.\"", "Quote by John Lennon: \"When I was 5 years old, my mother always told me that happiness was the key to life.  When I went to school, they asked me what I wanted to be when I grew up.  I wrote down ‘happy’.  They told me I didn’t understand the assignment, and I told them they didn’t understand life.\"", "Quote by Japanese Proverb: \"Fall seven times and stand up eight.\"", "Quote by Helen Keller: \"When one door of happiness closes, another opens, but often we look so long at the closed door that we do not see the one that has been opened for us.\"", "Quote by Confucius: \"Everything has beauty, but not everyone can see.\"", "Quote by Anne Frank: \"How wonderful it is that nobody need wait a single moment before starting to improve the world.\"", "Quote by Lao Tzu: \"When I let go of what I am, I become what I might be.\"", "Quote by Maya Angelou: \"Life is not measured by the number of breaths we take, but by the moments that take our breath away.\"", "Quote by Dalai Lama: \"Happiness is not something readymade.  It comes from your own actions.\"", "Quote by Sheryl Sandberg: \"If you’re offered a seat on a rocket ship, don’t ask what seat! Just get on.\"", "Quote by Aristotle: \"First, have a definite, clear practical ideal; a goal, an objective. Second, have the necessary means to achieve your ends; wisdom, money, materials, and methods. Third, adjust all your means to that end.\"", "Quote by Latin Proverb: \"If the wind will not serve, take to the oars.\"", "Quote by Unknown: \"You can’t fall if you don’t climb.  But there’s no joy in living your whole life on the ground.\"", "Quote by Marie Curie: \"We must believe that we are gifted for something, and that this thing, at whatever cost, must be attained.\"", "Quote by Les Brown: \"Too many of us are not living our dreams because we are living our fears.\"", "Quote by Joshua J. Marine: \"Challenges are what make life interesting and overcoming them is what makes life meaningful.\"", "Quote by Booker T. Washington: \"If you want to lift yourself up, lift up someone else.\"", "Quote by Leonardo da Vinci: \"I have been impressed with the urgency of doing. Knowing is not enough; we must apply. Being willing is not enough; we must do.\"", "Quote by Jamie Paolinetti: \"Limitations live only in our minds.  But if we use our imaginations, our possibilities become limitless.\"", "Quote by Erica Jong: \"You take your life in your own hands, and what happens? A terrible thing, no one to blame.\"", "Quote by Bob Dylan: \"What’s money? A man is a success if he gets up in the morning and goes to bed at night and in between does what he wants to do.\"", "Quote by Benjamin Franklin: \"I didn’t fail the test. I just found 100 ways to do it wrong.\"", "Quote by Bill Cosby: \"In order to succeed, your desire for success should be greater than your fear of failure.\"", "Quote by  Albert Einstein: \"A person who never made a mistake never tried anything new.\"", "Quote by Chinese Proverb: \"The person who says it cannot be done should not interrupt the person who is doing it.\"", "Quote by Roger Staubach: \"There are no traffic jams along the extra mile.\"", "Quote by George Eliot: \"It is never too late to be what you might have been.\"", "Quote by Oprah Winfrey: \"You become what you believe.\"", "Quote by Vincent van Gogh: \"I would rather die of passion than of boredom.\"", "Quote by Unknown: \"A truly rich man is one whose children run into his arms when his hands are empty.\"", "Quote by Ann Landers: \"It is not what you do for your children, but what you have taught them to do for themselves, that will make them successful human beings.\"", "Quote by Abigail Van Buren: \"If you want your children to turn out well, spend twice as much time with them, and half as much money.\"", "Quote by Farrah Gray: \"Build your own dreams, or someone else will hire you to build theirs.\"", "Quote by Jesse Owens: \"The battles that count aren’t the ones for gold medals. The struggles within yourself–the invisible battles inside all of us–that’s where it’s at.\"", "Quote by Sir Claus Moser: \"Education costs money.  But then so does ignorance.\"", "Quote by Rosa Parks: \"I have learned over the years that when one’s mind is made up, this diminishes fear.\"", "Quote by Confucius: \"It does not matter how slowly you go as long as you do not stop.\"", "Quote by Oprah Winfrey: \"If you look at what you have in life, you’ll always have more. If you look at what you don’t have in life, you’ll never have enough.\"", "Quote by Dalai Lama: \"Remember that not getting what you want is sometimes a wonderful stroke of luck.\"", "Quote by Maya Angelou: \"You can’t use up creativity.  The more you use, the more you have.\"", "Quote by Norman Vaughan: \"Dream big and dare to fail.\"", "Quote by Martin Luther King Jr.: \"Our lives begin to end the day we become silent about things that matter.\"", "Quote by Teddy Roosevelt: \"Do what you can, where you are, with what you have.\"", "Quote by Tony Robbins: \"If you do what you’ve always done, you’ll get what you’ve always gotten.\"", "Quote by Gloria Steinem: \"Dreaming, after all, is a form of planning.\"", "Quote by Mae Jemison: \"It’s your place in the world; it’s your life. Go on and do all you can with it, and make it the life you want to live.\"", "Quote by Beverly Sills: \"You may be disappointed if you fail, but you are doomed if you don’t try.\"", "Quote by Eleanor Roosevelt: \"Remember no one can make you feel inferior without your consent.\"", "Quote by Grandma Moses: \"Life is what we make it, always has been, always will be.\"", "Quote by Ayn Rand: \"The question isn’t who is going to let me; it’s who is going to stop me.\"", "Quote by Henry Ford: \"When everything seems to be going against you, remember that the airplane takes off against the wind, not with it.\"", "Quote by Abraham Lincoln: \"It’s not the years in your life that count. It’s the life in your years.\"", "Quote by Norman Vincent Peale: \"Change your thoughts and you change your world.\"", "Quote by Benjamin Franklin: \"Either write something worth reading or do something worth writing.\"", "Quote by –Audrey Hepburn: \"Nothing is impossible, the word itself says, “I’m possible!”\"", "Quote by Steve Jobs: \"The only way to do great work is to love what you do.\"", "Quote by Zig Ziglar: \"If you can dream it, you can achieve it.\""};
        Random random = new Random();
        return quotes[random.nextInt(quotes.length)];
    }

}
