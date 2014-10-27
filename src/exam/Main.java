/*********************************************************************************
 * This program, which is called Exam, is a simple program that simulates        *
 * the JAVA Certification Examination. First you would have to provide your user *
 * id and your four-digit password to start the exam. If the user does not exist,*
 * an error message will pump up. If the user has taken the exam twice, the user *
 * is not allawed to retake the exam and an error message will pump up.          *
 * Otherwise, a preparation message will pump up to inform the user that he/she  *
 * has 10 minutes to answer 20 questions about JAVA. The exam will start as soon *
 * as the user presses ENTER. The user id, password, and the number of exams     *
 * taken by an user are stored in a database table called user. All the questions*
 * that the program uses are stored in another table called questions.           *
 * All questions are devided in four levels of difficulty, level one the lowest  *
 * and level four the highest.                                                   *
 * The very first question has to be randomly selected. If the user answers that *
 * questions correctly the next question will be selected from the next level.   *
 * If the user answers that questions incorrectly the next question will be      *
 * selected from a lower level. If the user answers correctly a question from    *
 * level four the next question will be from level four. On the otherside,       *
 * if the user answers incorrectly a question from level one the next question   *
 * will be from level one.                                                       *
 * The time is going to be an issue here. If the user does not answer 20         *
 * questions in 10 minutes the exam will be terminated automatically and the     *
 * unanswered questions will count as incorrect questions. In case that the      *
 * user finishes very fast and he/she has a couple of minutes left, he/she       *
 * can terminate the program by clicking the "Submit" Button.                    *
 * Moreover, when the exam is considered terminated, there will be an output     *
 * that shows the user his score and the title he gets. If the user get a score  *
 * between 65 and 74.9 the user gets the title of JAVA Certified Programmer. If  *
 * the user gets between 75 and 84,9 the user gets the title of JAVA Certified   *
 * Developer. And finally if the user gets the a score of 85 or grater the user  *
 * gets the title of JAVA Certified Architect.                                   *
 *********************************************************************************
 *
 *
 * Developed by         : Santiago De La Torre
 * Last Modification    : Dic-12-2010
 * Educaitonal Center   : Bunker Hill Community Collefe.
 *
 */
package exam;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.swing.*;

public class Main extends JFrame {

//Interface
    JTextField user = new JTextField();
    JPasswordField password = new JPasswordField();
    JLabel ID = new JLabel("ID: ");
    JLabel passLabel = new JLabel("Password: ");
    JLabel cTime = new JLabel("Current Time: ");
    JLabel lTime = new JLabel("Time Left: ");
    JLabel titleBanner = new JLabel("Your Title is: ");
    JLabel scoreBanner = new JLabel("Your Score: ");
    JLabel lCurrentQst = new JLabel("Current Question: 1");
    JButton loggin = new JButton("Log in");
    JButton nextButton = new JButton("Next");
    JButton previousButton = new JButton("Previous");
    JButton submitButton = new JButton("Submit");
    JButton mainButton = new JButton("Main");
    JPanel logo = new JPanel();
    JLabel image = new JLabel(new ImageIcon("headerLogo.gif"));
    JLabel question = new JLabel();
    JRadioButton optionA = new JRadioButton();
    JRadioButton optionB = new JRadioButton();
    JRadioButton optionC = new JRadioButton();
    JRadioButton optionD = new JRadioButton();
    JTextArea code = new JTextArea(5, 30);
    JScrollPane scrollPane = new JScrollPane(code);
//panels
    JPanel introPanel = new JPanel();
    JPanel questionsPanel = new JPanel();
    JPanel scorePanel = new JPanel();
//Thread pool
    ExecutorService executor = Executors.newFixedThreadPool(3);
//Variables
    private int width = 1000, height = 700;
    private Clock clock = new Clock(true);
    private Clock cDown = new Clock(false);
    private String Database = "jdbc:mysql://localhost:3306/javaexam";
    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultset = null;
    private ResultSetMetaData metaData = null;
    private int nColumns = 0;
    private String line = null;
    private int iRndQuestion;
    private Random random = new Random();
    private int iQstSequence[] = new int[20];
    private int iCurrentQst = 0;
    private int iLevel = 0;
    private int iAsk[][] = new int[20][4];//0-19  0-3
    private int iAnswered[] = new int[20];
    private boolean isCorrect[] = new boolean[20];
    private String userID = new String();
    /* The next runnable is used to keep a trace of the time. If the time gets
     * over the exam will be terminated.
     * [PSEUDO CODE]
     * while this thread is executing
     *  check if the minutes and secods of the countdouwn clock are zero.
     *      if so, click the submit button and break the while.
     * Let's wait a second to recheck
     */
    public Runnable run = new Runnable() {

        public void run() {
            while (true) {
                if (cDown.imin == 0 && cDown.isec == 0) {
                    submitButton.doClick();
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };

    public Main() {

        setTitle("Java Exam");
        setSize(width, height);
        setLocation(width / 7, height / 10);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        clock.setBounds(900, 50, 80, 30);
        cDown.setBounds(900, 90, 80, 30);

        //Put all the object in the client area
        previousButton.setBounds(300, 580, 100, 20);
        nextButton.setBounds(450, 580, 100, 20);
        submitButton.setBounds(450, 620, 100, 20);
        mainButton.setBounds(450, 620, 100, 20);
        previousButton.setVisible(false);
        nextButton.setVisible(false);
        submitButton.setVisible(false);
        mainButton.setVisible(false);

        /*Note: The way I am doing the ouput of the program is in three different
         *      panels; one for the questions, another one for the the intro(user
         *      id, password), and another one for the final output(score and
         *      title)
         */
        //QUESTION panel
        question.setBounds(0, 0, 810, 20);
        scrollPane.setBounds(0, 30, 500, 200);
        code.setEditable(false);
        lCurrentQst.setBounds(520, 30, 200, 20);
        optionA.setBounds(0, 240, 810, 20);
        optionB.setBounds(0, 260, 810, 20);
        optionC.setBounds(0, 280, 810, 20);
        optionD.setBounds(0, 300, 810, 20);
        questionsPanel.add(lCurrentQst);
        questionsPanel.add(question);
        questionsPanel.add(scrollPane);
        questionsPanel.add(optionA);
        questionsPanel.add(optionB);
        questionsPanel.add(optionC);
        questionsPanel.add(optionD);
        questionsPanel.setLayout(null);
        questionsPanel.setBounds(width / 2 - 410, height / 4, 810, 400);
        questionsPanel.setVisible(false);


        //INTRO panel
        introPanel.add(ID);
        introPanel.add(user);
        introPanel.add(passLabel);
        introPanel.add(password);
        introPanel.add(loggin);
        introPanel.setLayout(new GridLayout(0, 2, 10, 10));
        introPanel.setBounds(width / 2 - 140, height / 3, 280, 75);


        //SCORE pnael
        scorePanel.setLayout(new GridLayout(0, 1));
        scorePanel.add(titleBanner);
        scorePanel.add(scoreBanner);
        scorePanel.setBounds(width / 2 - 140, height / 3, 280, 75);
        scorePanel.setVisible(false);


        cDown.bFlag = false;
        cDown.setVisible(false);
        executor.execute(cDown);
        logo.setBounds((width / 2 - 90), 60, 200, 80);
        logo.add(image);
        cTime.setBounds(800, 50, 80, 30);
        lTime.setBounds(820, 90, 80, 30);
        executor.execute(clock);

        /*  Loggin button: Once you press these button, the following function
         *  is going to check if the user exist, then if he/she is allowd to
         *  take the exam(based on how many time has he/she taken)
         */
        loggin.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                iLevel = random.nextInt(4) + 1;
                getRandom();

                userID = user.getText();
                if (isUser(userID, password.getText())) {
                    if (take()) {
                        introPanel.setVisible(false);
                        previousButton.setVisible(true);
                        nextButton.setVisible(true);
                        submitButton.setVisible(true);
                        previousButton.setEnabled(false);
                        JOptionPane.showOptionDialog(null, "You have 10 minutes to"
                                + " answer 20 questions\nPlease press ENTER to start the exam.", "Start",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

                        getQuestion();
                        cDown.setVisible(true);
                        cDown.setSeconds(0);
                        cDown.setMinute(10);

                        cDown.bFlag = true;
                        questionsPanel.setVisible(true);
                        executor.execute(run);

                    } else {
                        JOptionPane.showOptionDialog(null, "This user is not allowed to retake the exam", "Not allowed",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
                    }
                } else {
                    JOptionPane.showOptionDialog(null, "This user does not exist!\nPlease try it again!", "Input Error",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

                }
                user.setText(null);
                password.setText(null);

            }
        });

        /* Next Button: First is going to check if the current answer is correct
         * if so, increment one leve, otherwise decrement on level. And get
         * the next question Randomly.
         */
        nextButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (isCorrect[iCurrentQst]) {
                    incrementLevel();
                } else {
                    decrementLevel();
                }


                iCurrentQst++;

                if (iCurrentQst > 19) {
                    iCurrentQst = 19;

                    return;
                }

                getRandom();
                getQuestion();
                getSelectedAnswer();
                lCurrentQst.setText("Current Question: " + Integer.toString(iCurrentQst + 1));
            }
        });

        // Previous Button: Gets the previous question and its chosen answer.
        previousButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                iCurrentQst--;

                if (iCurrentQst < 0) {

                    iCurrentQst = 0;

                }
                getQuestion();
                getSelectedAnswer();
                lCurrentQst.setText("Current Question: " + Integer.toString(iCurrentQst + 1));
            }
        });

        //Sumbmit button: finish the exam.
        submitButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String text = new String();
                double x = 0;

                titleBanner.setText("Your Title is: ");
                scoreBanner.setText("Your Score: ");
                cDown.bFlag = false;
                questionsPanel.setVisible(false);
                previousButton.setVisible(false);
                nextButton.setVisible(false);
                mainButton.setVisible(true);

                x = getScore();
                submitButton.setVisible(false);
                text = scoreBanner.getText() + Double.toString(x);
                scoreBanner.setText(text);

                text = titleBanner.getText();

                if (x < 65) {
                    text += "Fail";
                }
                if (x >= 65 && x <= 74.9) {
                    text += "JAVA Certified Programmer";
                }
                if (x >= 75 && x <= 84.9) {
                    text += "JAVA Certified Developer";
                }
                if (x >= 85) {
                    text += "JAVA Certifeid Architect";
                }

                titleBanner.setText(text);
                scorePanel.setVisible(true);
            }
        });


        /*Main button: Once the exam is terminated, and the output for the
         * score and title are out there. you may wan to use this button
         * go back to main.
         */
        mainButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mainButton.setVisible(false);
                introPanel.setVisible(true);
                scorePanel.setVisible(false);
                cDown.setVisible(false);

                iCurrentQst = 0;
                for (int index = 0; index <= 19; index++) {
                    iQstSequence[index] = 0;
                    iAsk[index][0] = 0;
                    iAsk[index][1] = 0;
                    iAsk[index][2] = 0;
                    iAsk[index][3] = 0;

                    iAnswered[index] = 0;
                    isCorrect[index] = false;

                }
                getSelectedAnswer();


            }
        });

        optionA.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                optionA.setSelected(true);
                optionB.setSelected(false);
                optionC.setSelected(false);
                optionD.setSelected(false);
                iAnswered[iCurrentQst] = 1;
                try {
                    if (optionA.getText().equals(resultset.getObject(3).toString())) {
                        isCorrect[iCurrentQst] = true;
                    } else {
                        isCorrect[iCurrentQst] = false;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        optionB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                optionA.setSelected(false);
                optionB.setSelected(true);
                optionC.setSelected(false);
                optionD.setSelected(false);
                iAnswered[iCurrentQst] = 2;
                try {
                    if (optionB.getText().equals(resultset.getObject(3).toString())) {
                        isCorrect[iCurrentQst] = true;
                    } else {
                        isCorrect[iCurrentQst] = false;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        optionC.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                optionA.setSelected(false);
                optionB.setSelected(false);
                optionC.setSelected(true);
                optionD.setSelected(false);
                iAnswered[iCurrentQst] = 3;
                try {
                    if (optionC.getText().equals(resultset.getObject(3).toString())) {
                        isCorrect[iCurrentQst] = true;
                    } else {
                        isCorrect[iCurrentQst] = false;
                    }



                } catch (SQLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        optionD.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                optionA.setSelected(false);
                optionB.setSelected(false);
                optionC.setSelected(false);
                optionD.setSelected(true);
                iAnswered[iCurrentQst] = 4;
                try {
                    if (optionD.getText().equals(resultset.getObject(3).toString())) {
                        isCorrect[iCurrentQst] = true;
                    } else {
                        isCorrect[iCurrentQst] = false;
                    }


                } catch (SQLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        add(scorePanel);
        add(mainButton);
        add(submitButton);
        add(previousButton);
        add(nextButton);
        add(questionsPanel);
        add(introPanel);
        add(cTime);
        add(lTime);
        add(logo);
        add(cDown);
        add(clock);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }
    /* isUser function: checks if the user exist or not. If so, it will return
     * true otherwise it will return false.
     */

    public boolean isUser(String Id, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(Database, "santi", "santi");
            statement = connection.createStatement();
            resultset = statement.executeQuery("SELECT id, password FROM user");
            metaData = resultset.getMetaData();
            nColumns = metaData.getColumnCount();
            line = "";
            while (resultset.next()) {
                for (int i = 1; i <= nColumns; i++) {
                    line = "";
                    line = (String) resultset.getObject(i);
                    if (line.equals(Id)) {
                        line = (String) resultset.getObject(i + 1);
                        if (line.equals(password)) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException ent) {
            System.out.println("SQL Exception: " + ent.toString());
        } catch (ClassNotFoundException cE) {
            System.out.println("Class Not Found Exception: " + cE.toString());
        }
        return false;
    }

    //getQuestion gets the question from the database and puts in the client area
    public void getQuestion() {
        int i = 0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(Database, "santi", "santi");
            statement = connection.createStatement();
            resultset = statement.executeQuery("SELECT  N,Question, Answer,Option1,Option2,Option3, code FROM questions");

            line = "";

            while (resultset.next()) {
                i = Integer.parseInt(resultset.getObject(1).toString());
                if (i == iQstSequence[iCurrentQst]) {
                    break;
                }
            }

            line = "";
            line = resultset.getObject(2).toString();
            question.setText(line);

            line = resultset.getObject(iAsk[iCurrentQst][0]).toString();
            optionA.setText(line);

            line = resultset.getObject(iAsk[iCurrentQst][1]).toString();
            optionB.setText(line);
            line = resultset.getObject(iAsk[iCurrentQst][2]).toString();
            optionC.setText(line);

            line = resultset.getObject(iAsk[iCurrentQst][3]).toString();
            optionD.setText(line);

            line = (String) resultset.getObject(7);

            if (line != null) {
                code.setText(line);
                scrollPane.setVisible(true);

            } else {
                scrollPane.setVisible(false);
            }

        } catch (SQLException ent) {
            System.out.println("SQL Exception: " + ent.toString());
        } catch (ClassNotFoundException cE) {
            System.out.println("Class Not Found Exception: " + cE.toString());
        }

        questionsPanel.repaint();
    }
    //gerRandom function gets a random number based on the level you are.

    public void getRandom() {

        if (iQstSequence[iCurrentQst] != 0) {
            return;
        }

        switch (iLevel) {

            case 1:
                iRndQuestion = random.nextInt(25) + 1;
                while (isRepeated(iRndQuestion)) {
                    iRndQuestion = random.nextInt(25) + 1;
                }
                break;

            case 2:
                iRndQuestion = random.nextInt(25) + 26;
                while (isRepeated(iRndQuestion)) {
                    iRndQuestion = random.nextInt(25) + 26;
                }
                break;

            case 3:
                iRndQuestion = random.nextInt(25) + 51;
                while (isRepeated(iRndQuestion)) {
                    iRndQuestion = random.nextInt(25) + 51;
                }
                break;

            case 4:
                iRndQuestion = random.nextInt(25) + 76;
                while (isRepeated(iRndQuestion)) {
                    iRndQuestion = random.nextInt(25) + 76;
                }
        }
        iQstSequence[iCurrentQst] = iRndQuestion;
        for (int index = 0; index < 4; index++) {
            iAsk[iCurrentQst][index] = random.nextInt(4) + 3;
        }

        for (int second = 0; second < 4; second++) {
            for (int index = 0; index < 4; index++) {
                if (second != index) {
                    if (iAsk[iCurrentQst][second] == iAsk[iCurrentQst][index]) {
                        iAsk[iCurrentQst][second] = random.nextInt(4) + 3;
                        second = 0;
                    }
                }
            }
        }
    }

    //isRepeated function checks if a question has been asked
    public boolean isRepeated(int questionNumber) {

        for (int index = 0; index <= iCurrentQst; index++) {
            if (iQstSequence[index] == questionNumber) {
                return true;
            }
        }
        return false;
    }

    /* getSelectedAnser: is gonna choose the radio button whic answer has been
     * chosen.
     * This function is essential in case the user wants to go back to a
     * question, the answer has to be the same as he/she answered.
     */
    public void getSelectedAnswer() {
        previousButton.setEnabled(true);
        nextButton.setEnabled(true);
        optionA.setSelected(false);
        optionB.setSelected(false);
        optionC.setSelected(false);
        optionD.setSelected(false);
        switch (iAnswered[iCurrentQst]) {
            case 1:
                optionA.setSelected(true);
                break;
            case 2:
                optionB.setSelected(true);
                break;
            case 3:
                optionC.setSelected(true);
                break;

            case 4:
                optionD.setSelected(true);
                break;
        }
        if (iCurrentQst == 0) {
            previousButton.setEnabled(false);
        }
        if (iCurrentQst == 19) {
            nextButton.setEnabled(false);
        }
    }

    public void incrementLevel() {
        iLevel++;
        if (iLevel > 4) {
            iLevel = 4;
        }
    }

    public void decrementLevel() {
        iLevel--;
        if (iLevel < 1) {
            iLevel = 1;
        }
    }

    /* getScore fucntion: performs the mathematical operations provided by the
     * specifications of the program.
     * This function calculates the final grade of the user's performance in
     * the exam.
     */
    public double getScore() {
        double iCorrect = 0, iIncorrect = 0, iScore = 0, iPenalty = 0, iNPoints = 0;

        for (int index = 0; index < isCorrect.length; index++) {
            if (isCorrect[index]) {
                iCorrect++;
            } else {
                iIncorrect++;
            }
        }
        iPenalty = (20 - iIncorrect) / 20;
        iNPoints = ((1 - iPenalty) * iIncorrect) + iIncorrect;
        if (iIncorrect > 0) {
            iScore = (iCorrect * iIncorrect) - iNPoints;
        } else {
            iScore = iCorrect / 20 * 100;
        }
        if (iScore < 0) {
            return 0;
        }
        return iScore;
    }

    /* take function checks how many time has the user taken the exam.
     * If the user has taken it more then once it returns true. Otherwise,
     * the number of exams taken will be incremented in the database.
     */
    public boolean take() {
        try {
            int iExam = 0;
            resultset = statement.executeQuery("SELECT id, exam FROM user");
            line = "";
            while (resultset.next()) {
                line = (String) resultset.getObject(1);
                if (userID.equals(line)) {
                    iExam = Integer.parseInt(resultset.getObject(2).toString());
                    if (iExam == 2) {
                        return false;
                    } else {
                        iExam++;
                        statement.executeUpdate("UPDATE user SET exam ='" + iExam + "' WHERE id = '" + userID + "'");
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
