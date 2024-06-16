package br.gmacspm.mdtbattery;

import org.junit.Test;

import static org.junit.Assert.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import br.gmacspm.mdtbattery.models.UsageModel;
import br.gmacspm.mdtbattery.utils.TimeConverter;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */


public class ExampleUnitTest {
    @Test
    public void isTimeConvertOK(){
        assertEquals("00m01s", TimeConverter.getHumanTime(1000, false));
        assertEquals("00m59s", TimeConverter.getHumanTime(59999, false));

        assertEquals("01m00s", TimeConverter.getHumanTime(60000, false));
        assertEquals("59m59s", TimeConverter.getHumanTime(3599999, false));

        assertEquals("01h00m", TimeConverter.getHumanTime(3600000, false));
        assertEquals("23h59m", TimeConverter.getHumanTime(86399999, false));

        assertEquals("01d00h", TimeConverter.getHumanTime(86400000, false));
        assertEquals("01d01h", TimeConverter.getHumanTime(90000000, false));

    }
}