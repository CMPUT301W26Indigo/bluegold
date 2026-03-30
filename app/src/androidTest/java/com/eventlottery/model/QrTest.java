package com.eventlottery.model;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import com.eventlottery.ui.qr.QRScannerActivity;

import org.junit.Before;
import org.junit.Test;

public class QrTest {

    @Before
    public void setUp() {
        event = new Event();
        event.setId("test_id");
        event.setName("Test Event");
    }
    private Event event;
    @Test
    public void testGenerateQRLink() {
        Bitmap qrCode = event.generateQR();

        assertNotNull(qrCode);
        assertTrue(qrCode.getWidth() > 0);
        assertTrue(qrCode.getHeight() > 0);
    }

    @Test
    public void testNoTwoEventsTheSame() {
        Event event2 = new Event();
        event2.setId("test_id2");
        event2.setName("Test Event 2");
        Bitmap qrCode = event.generateQR();
        Bitmap qrCode2 = event2.generateQR();
        assertFalse(qrCode.sameAs(qrCode2));
    }

    @Test
    public void testGenerateQrFail() {

    }
    @Test
    public void testScanQrUISuccess() {
        init();

        Intent resultData = new Intent();
        resultData.putExtra("SCAN_RESULT", "eventlottery://event/test_id");

        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        intending(IntentMatchers.anyIntent()).respondWith(result);

        ActivityScenario.launch(QRScannerActivity.class);

        onView(withText("Event Found"))
                .check(matches(isDisplayed()));

        release();
    }
    @Test
    public void testScanQrUIFail() {
        init();

        Intent resultData = new Intent();
        resultData.putExtra("SCAN_RESULT", "invalid_qr_code");

        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        intending(IntentMatchers.anyIntent()).respondWith(result);

        ActivityScenario.launch(QRScannerActivity.class);

        onView(withText("Invalid QR Code")).check(matches(isDisplayed()));

        release();
    }
    @Test
    public void testScanQrUICancel() {
        init();

        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null);

        intending(IntentMatchers.anyIntent()).respondWith(result);

        ActivityScenario.launch(QRScannerActivity.class);

        onView(withText("Scan cancelled"))
                .check(matches(isDisplayed()));

        release();
    }

}
