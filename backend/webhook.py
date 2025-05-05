import json
import os
import stripe
import requests
import ast 

from flask import Flask, jsonify, request

stripe.api_key = 'sk_test_51R65YnFQK7HmrpDOZ8MhB8waSHJG8dnhtt4oJGfiaFbhRW79rQ3dyz42r6GQkvd54jxQyl0en2pq13btXYQxuX0B008fDthRBe'
endpoint_secret = 'whsec_0zO4XvfTM1kJl1S6TrbfwuKgOZh3PL9D'

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
<<<<<<< HEAD
            print('  Webhook signature verification failed.' + str(e))
=======
            print('⚠️  Webhook signature verification failed.' + str(e))
>>>>>>> 54e63762880cba51b179e7b9d6c14d38264b3d60
            return jsonify(success=False)

    if event and event['type'] == 'payment_intent.succeeded':
        payment_intent = event['data']['object']  # Stripe PaymentIntent object

        email = payment_intent['metadata'].get('email')
        first_name = payment_intent['metadata'].get('first_name', '')
        last_name = payment_intent['metadata'].get('last_name', '')
        full_name = f"{first_name} {last_name}".strip()

        reservation_data_str = payment_intent['metadata'].get('reservations', '[]')
        try:
            reservations = ast.literal_eval(reservation_data_str)
        except:
            reservations = []

    
        send_confirmation_email(to_email=email, guest_name=full_name, reservations=reservations)
<<<<<<< HEAD
        print(' Payment for {} succeeded'.format(payment_intent['amount']))
=======
        print('✅ Payment for {} succeeded'.format(payment_intent['amount']))
>>>>>>> 54e63762880cba51b179e7b9d6c14d38264b3d60

    else:
        print('⚠️  Unhandled event type {}'.format(event['type']))

    return jsonify(success=True)

def send_confirmation_email(to_email, guest_name, reservations):
    api_key = "re_GGbAPqaf_7ixDqTRVz1koQnP1N97mGJ6v"

    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }

    reservation_html = ""
    for r in reservations:
        room_Id = r.get('roomId', 'N/A')  
        check_in = r.get('check_in', 'N/A')
        check_out = r.get('check_out', 'N/A')
        

        reservation_html += f"""
        <div style="margin-bottom: 20px;">
            <strong>Room ID:</strong> {room_Id}<br/>
            <strong>Check-in:</strong> {check_in}<br/>
            <strong>Check-out:</strong> {check_out}<br/>
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

    print("Email Status:", response.status_code)
    print("API Key:", api_key)

    print("Email Response:", response.json())


if __name__ == '__main__':
    app.run(port=3000, debug=True)