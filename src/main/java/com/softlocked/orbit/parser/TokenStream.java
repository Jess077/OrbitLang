package com.softlocked.orbit.parser;

import java.util.ArrayList;
import java.util.List;

public class TokenStream {
    private List<String> tokens;
    private int position;

    public TokenStream(List<String> tokens) {
        this.tokens = tokens;
        this.tokens.removeIf(t -> t.equals("\r") || t.equals("\n") || t.equals("\t"));
        this.position = 0;
    }

    public boolean hasNext() {
        return position < tokens.size();
    }

    public String next() {
        if (!hasNext()) {
            return null;
        }
        return tokens.get(position++);
    }

    public void consumeSemicolon() {
        if (match(";")) {
            next();
        }
    }

    public String peek(int offset) {
        if (position + offset >= tokens.size()) {
            return null;
        }
        return tokens.get(position + offset);
    }

    public String peek() {
        return peek(0);
    }

    public String expect(String expected) {
        if (!hasNext()) {
            throw new RuntimeException("Unexpected end of file, expected: " + expected);
        }
        String token = next();
        if (!token.equals(expected)) {
            throw new RuntimeException("Expected token: " + expected + ", but found: " + token);
        }
        return token;
    }

    public boolean match(String token) {
        return peek() != null && peek().equals(token);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }

    public int findNext(String target) {
        for (int i = position; i < tokens.size(); i++) {
            if (tokens.get(i).equals(target)) {
                return i;
            }
        }
        return -1;
    }

    public int findPair(String open, String close) {
        int depth = 1;
        int startPos = position;

        for (int i = startPos; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.equals(open)) depth++;
            if (token.equals(close)) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    public List<String> getRange(int start, int end) {
        return new ArrayList<>(tokens.subList(start, end));
    }

    public TokenStream subStream(int start, int end) {
        return new TokenStream(getRange(start, end));
    }

    public int size() {
        return tokens.size();
    }

    public String get(int index) {
        return tokens.get(index);
    }

    public int cursor() {
        return position;
    }
}
