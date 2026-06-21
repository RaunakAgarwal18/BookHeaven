async function testWebhook() {
  try {
    const payload = {
      event: 'payment.captured',
      payload: {
        payment: {
          entity: {
            order_id: 'order_Sz1zVg4dpu1ZM0',
            id: 'mock_id',
            method: 'mock',
            amount: 117552
          }
        }
      }
    };
    
    console.log("Sending request...");
    const response = await fetch('http://localhost:8090/api/payment/webhook', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Razorpay-Signature': 'bypass-signature-for-testing'
      },
      body: JSON.stringify(payload)
    });
    console.log("Success:", response.status);
    if (!response.ok) {
      const text = await response.text();
      console.error("Error data:", text);
    }
  } catch (err) {
    console.error("Failed:", err.message);
  }
}

testWebhook();
