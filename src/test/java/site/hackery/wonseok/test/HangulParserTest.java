package site.hackery.wonseok.test;

import org.junit.Assert;
import org.junit.Test;
import site.hackery.wonseok.util.HangulParser;

public class HangulParserTest {
    @Test
    public void hello() {
        assertCharArraysEqual(new char[] { 'ㅇ', 'ㅏ', 'ㄴ' }, HangulParser.deconstructNonAtomic('안'));
        assertCharArraysEqual(new char[] { 'ㄴ', 'ㅕ', 'ㅇ' }, HangulParser.deconstructNonAtomic('녕'));

        Assert.assertEquals("안녕하세요", HangulParser.construct(new char[] { 'ㅇ', 'ㅏ', 'ㄴ', 'ㄴ', 'ㅕ', 'ㅇ', 'ㅎ', 'ㅏ', 'ㅅ', 'ㅔ', 'ㅇ', 'ㅛ' }));
    }

    @Test
    public void constructFinalConsonantShift() {
        Assert.assertEquals("하세", HangulParser.construct(new char[] { 'ㅎ', 'ㅏ', 'ㅅ', 'ㅔ' }));
    }

    @Test
    public void constructVowelJoin() {
        Assert.assertEquals("우와", HangulParser.construct(new char[] { 'ㅇ', 'ㅜ', 'ㅇ', 'ㅗ', 'ㅏ' }));
    }

    @Test
    public void finickyConsonantJoin() {
        // With a naïve consonant joiner, the produced construction would be '마ㅀㅐ'
        // We've gotten around this by not joining if there is a vowel after our digraph, but is there a better way?
        Assert.assertEquals("말해", HangulParser.construct(new char[] { 'ㅁ', 'ㅏ', 'ㄹ', 'ㅎ', 'ㅐ' }));
    }

    @Test
    public void appendDeadVowel() {
        Assert.assertEquals("아ㅏ", HangulParser.construct(new char[] { 'ㅇ', 'ㅏ', 'ㅏ' }));
    }

    private static void assertCharArraysEqual(char[] actual, char[] expected) {
        Assert.assertEquals(new String(actual), new String(expected));
    }
}
