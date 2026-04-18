package com.example.quizapp;

import java.io.Serializable;

public class Models {

    public static class User {
        public int id;
        public String username;
        public String password;
        public String role;

        public User(int id, String username, String password, String role) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.role = role;
        }
    }

    public static class Question implements Serializable {
        public int id;
        public String question;
        public String[] options;
        public int answerIndex;
        public String category;
        public String difficulty;

        public Question(int id, String question, String[] options,
                        int answerIndex, String category, String difficulty) {
            this.id = id;
            this.question = question;
            this.options = options;
            this.answerIndex = answerIndex;
            this.category = category;
            this.difficulty = difficulty;
        }
    }

    public static class Attempt {
        public int id;
        public String username;
        public int score;
        public String date;
        public String category;
        public int correct;
        public int wrong;
        public long timeTaken;

        public Attempt(int id, String username, int score, String date,
                       String category, int correct, int wrong, long timeTaken) {
            this.id = id;
            this.username = username;
            this.score = score;
            this.date = date;
            this.category = category;
            this.correct = correct;
            this.wrong = wrong;
            this.timeTaken = timeTaken;
        }
    }

    public static class AttemptDetail {
        public String question;
        public String selectedOption;
        public String correctOption;
        public boolean isCorrect;

        public AttemptDetail(String question, String selectedOption,
                             String correctOption, boolean isCorrect) {
            this.question = question;
            this.selectedOption = selectedOption;
            this.correctOption = correctOption;
            this.isCorrect = isCorrect;
        }
    }

    // ✅ NEW: Badge model
    public static class Badge {
        public int id;
        public String username;
        public String badgeKey;
        public String badgeName;
        public String badgeDescription;
        public String earnedDate;

        public Badge(int id, String username, String badgeKey,
                     String badgeName, String badgeDescription, String earnedDate) {
            this.id = id;
            this.username = username;
            this.badgeKey = badgeKey;
            this.badgeName = badgeName;
            this.badgeDescription = badgeDescription;
            this.earnedDate = earnedDate;
        }
    }
}
