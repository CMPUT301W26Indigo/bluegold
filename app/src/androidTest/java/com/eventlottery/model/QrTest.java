package com.eventlottery.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.eventlottery.ui.qr.QRDisplayActivity;
import com.eventlottery.ui.qr.QRScannerActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for QR code functionality, including generation logic in the Event model
 * and activity launching.
 */
@RunWith(AndroidJUnit4.class)
public class QrTest {

    /**
     * Verifies that a QR bitmap is correctly generated for a public event.
     */
    @Test
    public void testGenerateQRBitmap_Success() {
        Event event = new Event();
        event.setPrivate(false);
        Bitmap bitmap = event.generateQRBitmap("event_id_123");
        assertNotNull("QR Bitmap should be generated for public events", bitmap);
    }

    /**
     * Verifies that QR generation returns null for private events to protect privacy.
     */
    @Test
    public void testGenerateQRBitmap_PrivateEvent() {
        Event event = new Event();
        event.setPrivate(true);
        Bitmap bitmap = event.generateQRBitmap("event_id_123");
        assertNull("QR Bitmap should be null for private events", bitmap);
    }

    /**
     * Verifies that QR generation returns null when content is null.
     */
    @Test
    public void testGenerateQRBitmap_NullContent() {
        Event event = new Event();
        event.setPrivate(false);
        Bitmap bitmap = event.generateQRBitmap(null);
        assertNull("QR Bitmap should be null for null content", bitmap);
    }

    /**
     * Verifies that QR generation returns null when content is empty.
     */
    @Test
    public void testGenerateQRBitmap_EmptyContent() {
        Event event = new Event();
        event.setPrivate(false);
        Bitmap bitmap = event.generateQRBitmap("");
        assertNull("QR Bitmap should be null for empty content", bitmap);
    }

    /**
     * Tests the successful launch of QRScannerActivity.
     */
    @Test
    public void testScannerActivityLaunch() {
        try (ActivityScenario<QRScannerActivity> scenario = ActivityScenario.launch(QRScannerActivity.class)) {
            assertNotNull(scenario);
        }
    }

    /**
     * Tests the successful launch of QRDisplayActivity with an event ID.
     */
    @Test
    public void testDisplayActivityLaunch() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, QRDisplayActivity.class);
        intent.putExtra("EVENT_ID", "test_event_id");
        try (ActivityScenario<QRDisplayActivity> scenario = ActivityScenario.launch(intent)) {
            assertNotNull(scenario);
        }
    }
}
