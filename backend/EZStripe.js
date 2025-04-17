const express = require('express');
const stripe = require('stripe')('sk_test_51R65YnFQK7HmrpDOZ8MhB8waSHJG8dnhtt4oJGfiaFbhRW79rQ3dyz42r6GQkvd54jxQyl0en2pq13btXYQxuX0B008fDthRBe');

const app = express();
const port = 5000;

app.use(express.json());

console.log('Setting up the payment sheet route...');

app.post('/payment-sheet', async (req, res) => {
  try {
    console.log('Received payment-sheet request...');
    
    const customer = await stripe.customers.create();
    const ephemeralKey = await stripe.ephemeralKeys.create(
      {customer: customer.id},
      {apiVersion: '2025-02-24.acacia'}
    );
    const paymentIntent = await stripe.paymentIntents.create({
      amount: 1099,
      currency: 'eur',
      customer: customer.id,
      automatic_payment_methods: { enabled: true },
    });

    console.log('Payment sheet response ready, sending...');
    
    res.json({
      paymentIntent: paymentIntent.client_secret,
      ephemeralKey: ephemeralKey.secret,
      customer: customer.id,
      publishableKey: 'pk_test_51R65YnFQK7HmrpDOoWGJb8R1Ibtx6yKxA7Vue9hK0yzx0IMUerpXXO6YaiYC0wxLhTar6AUtNAd5nONMsbXQCK9T00KbjRuJL5'
    });
  } catch (error) {
    console.error('Error during payment creation:', error);
    res.status(500).send('Something went wrong');
  }
});

console.log('Starting server...');
app.listen(port, () => {
  console.log(`Server running at ${port}`);
});
