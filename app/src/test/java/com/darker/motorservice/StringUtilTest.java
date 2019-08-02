package com.darker.motorservice;

import com.darker.motorservice.utility.StringUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Darker on 14/11/60.
 */

public class StringUtilTest {
    @Test
    public void stringNotNull_isCorrect(){
        Assert.assertTrue(StringUtil.isStringOk("1234"));
    }

    @Test
    public void stringNull_isNotCorrect(){
        Assert.assertFalse(StringUtil.isStringOk(null));
    }

    @Test
    public void stringEmpty_isNotCorrect(){
        Assert.assertFalse(StringUtil.isStringOk(""));
    }

    @Test
    public void phoneNumber_9digit_isCorrect(){
        Assert.assertTrue(StringUtil.isPhoneNumber("123456789"));
    }

    @Test
    public void phoneNumber_10digit_isCorrect(){
        Assert.assertTrue(StringUtil.isPhoneNumber("1234567890"));
    }

    @Test
    public void phoneNumber_moreThan10digit_isNotCorrect(){
        Assert.assertFalse(StringUtil.isPhoneNumber("12345678901"));
    }

    @Test
    public void phoneNumber_lessThan9digit_isNotCorrect(){
        Assert.assertFalse(StringUtil.isPhoneNumber("12345678"));
    }

    @Test
    public void phoneNumber_textString_isNotCorrect(){
        Assert.assertFalse(StringUtil.isPhoneNumber("test"));
    }

    @Test
    public void phoneNumber_textEmpty_isNotCorrect(){
        Assert.assertFalse(StringUtil.isPhoneNumber(""));
    }

    @Test
    public void phoneNumberNull_isNotCorrect(){
        Assert.assertFalse(StringUtil.isPhoneNumber(null));
    }

    @Test
    public void email_isCorrect(){
        String email = "test@mail.com";
        Assert.assertTrue(StringUtil.isEmail(email));
    }

    @Test
    public void email_null_isNotCorrect(){
        String email = null;
        Assert.assertFalse(StringUtil.isEmail(email));
    }

    @Test
    public void email_textEmpty_isNotCorrect(){
        String email = "";
        Assert.assertFalse(StringUtil.isEmail(email));
    }

    @Test
    public void email_normalText_isNotCorrect(){
        String email = "qweqw";
        Assert.assertFalse(StringUtil.isEmail(email));
    }

    @Test
    public void email_wrongFormat_isNotCorrect(){
        String email = "a@mail";
        Assert.assertFalse(StringUtil.isEmail(email));
    }

    @Test
    public void email_wrongFormatWithDot_isNotCorrect(){
        String email = "a@mail.";
        Assert.assertFalse(StringUtil.isEmail(email));
    }

    @Test
    public void email_notAt_isNotCorrect(){
        String email = "mail.com";
        Assert.assertFalse(StringUtil.isEmail(email));
    }
}
