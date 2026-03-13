package com.example.bluegold;

import android.graphics.Bitmap;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
// import com.eventlottery.ui.qr.QRScannerActivity;
import com.eventlottery.model.Event;

@RunWith(MockitoJUnitRunner.class)
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

    }
    @Test
    public void testScanQrUIFail() {

    }
    @Test
    public void testScanQrUICancel() {

    }

}
