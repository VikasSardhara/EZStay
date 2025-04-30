from flask import Flask, request, jsonify
import stripe
import json


app = Flask(name)
stripe.api_key = 'sk_test_51R65YnFQK7HmrpDOZ8MhB8waSHJG8dnhtt4oJGfiaFbhRW79rQ3dyz42r6GQkvd54jxQyl0en2pq13btXYQxuX0B008fDthRBe'

@app.route('/payment-sheet', methods=['POST'])
def payment_sheet():
    try:
        data = request.get_json()
        print("Received payment-sheet request...")
        print("Request body:", data)

        amount = data.get('amount')
        first_name = data.get('first_name')
        last_name = data.get('last_name')
        email = data.get('email')
        reservations = data.get('reservations', [])

        # Create customer
        customer = stripe.Customer.create(
            email= email,
            name=f"{first_name} {last_name}",
        )

        # Create ephemeral key
        ephemeralKey = stripe.EphemeralKey.create(
            customer=customer['id'],
            stripe_version='2025-02-24.acacia',
        )

        paymentIntent = stripe.PaymentIntent.create(
            amount=amount * 10,
            metadata={
                "amount": str(amount),
                "first_name": first_name,
                "last_name": last_name,
                "email": email,
                "reservations": json.dumps(reservations)
                },
            currency='usd',
            customer=customer['id'],
            automatic_payment_methods={'enabled': True},
        )

        return jsonify(
            paymentIntent=paymentIntent.client_secret,
            ephemeralKey=ephemeralKey.secret,
            customer=customer.id,
            publishableKey='pk_test_51R65YnFQK7HmrpDOoWGJb8R1Ibtx6yKxA7Vue9hK0yzx0IMUerpXXO6YaiYC0wxLhTar6AUtNAd5nONMsbXQCK9T00KbjRuJL5'
        )

    except Exception as e:
        print("Error during payment creation:", e)
        return jsonify({"error": str(e)}), 500


if name == "main":
    app.run(debug=True, host="0.0.0.0", port=4242)