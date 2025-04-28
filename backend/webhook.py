import json
import os
import stripe
import requests
import ast  # for safely evaluating the stringified reservation list

from flask import Flask, jsonify, request

stripe.api_key = 'sk_test_51R65YnFQK7HmrpDOZ8MhB8waSHJG8dnhtt4oJGfiaFbhRW79rQ3dyz42r6GQkvd54jxQyl0en2pq13btXYQxuX0B008fDthRBe'
endpoint_secret = 'whsec_21440c884ebbe112507d7f91eebb3bf0a2758b142be938f559ad7cf0c51cc7ba'

app = Flask(__name__)

@app.route('/webhook', methods=['POST'])
def webhook():
    event = None
    payload = request.data

    try:
        event = json.loads(payload)
    except json.decoder.JSONDecodeError as e:
        print('⚠️  Webhook error while parsing basic request.' + str(e))
        return jsonify(success=False)

    if endpoint_secret:
        sig_header = request.headers.get('stripe-signature')
        try:
            event = stripe.Webhook.construct_event(
                payload, sig_header, endpoint_secret
            )
        except stripe.error.SignatureVerificationError as e:
            print('⚠️  Webhook signature verification failed.' + str(e))
            return jsonify(success=False)

    # Handle the event
    if event and event['type'] == 'payment_intent.succeeded':
        payment_intent = event['data']['object']  # Stripe PaymentIntent object

        email = payment_intent['metadata'].get('email')
        first_name = payment_intent['metadata'].get('first_name', '')
        last_name = payment_intent['metadata'].get('last_name', '')
        full_name = f"{first_name} {last_name}".strip()

        # Parse reservations from metadata (stringified JSON list)
        reservation_data_str = payment_intent['metadata'].get('reservations', '[]')
        try:
            reservations = ast.literal_eval(reservation_data_str)
        except:
            reservations = []

        # Send confirmation email
        send_confirmation_email(to_email=email, guest_name=full_name, reservations=reservations)
        print('✅ Payment for {} succeeded'.format(payment_intent['amount']))

    elif event['type'] == 'payment_method.attached':
        # Optional: handle this if needed
        payment_method = event['data']['object']
        print('✅ Payment method attached.')

    else:
        print('⚠️  Unhandled event type {}'.format(event['type']))

    return jsonify(success=True)


def send_confirmation_email(to_email, guest_name, reservations):
    api_key = os.environ.get("re_9Aq6x2gi_PsTdLo4xRD91pLSjUZoZgRfA")  # Your Resend API key

    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }

    # Build reservation details into HTML format
    reservation_html = ""
    for r in reservations:
        reservation_html += f"""
        <div style="margin-bottom: 20px;">
            <strong>Room Type:</strong> {r['room_type']}<br/>
            <strong>Smoking:</strong> {r['smoking']}<br/>
            <strong>Guests:</strong> {r['guests']}<br/>
            <strong>Check-in:</strong> {r['check_in']}<br/>
            <strong>Check-out:</strong> {r['check_out']}<br/>
            <strong>Price:</strong> ${r['price']}<br/>
        </div>
        """

    html_body = f"""
    <h2>Hi {guest_name},</h2>
    <p>Thanks for booking with <strong>EZStay</strong>!</p>
    <p>Here are your reservation details:</p>
    {reservation_html}
    <p>We look forward to hosting you! 🏨</p>
    """

    data = {
        "from": "EZStay <onboarding@resend.dev>",
        "to": to_email,
        "subject": "Your EZStay Booking Confirmation",
        "html": html_body
    }

    response = requests.post("https://api.resend.com/emails", headers=headers, json=data)

    print("📧 Email Status:", response.status_code)
    print("📧 Email Response:", response.json())


if __name__ == '__main__':
    app.run(port=3000, debug=True)
