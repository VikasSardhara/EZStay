"""
app.py

Main entry point of the backend server.

This file starts the Flask application, enables CORS, and registers routes
for rooms, bookings, and user functionalities.

Author: EZStay Backend Team
Date: April 2025
"""


from flask import Flask, jsonify
from flask_cors import CORS
from routes.rooms import rooms_bp
from routes.bookings import bookings_bp
from routes.users import users_bp
import os

app = Flask(__name__)
CORS(app)

# Register Routes
app.register_blueprint(rooms_bp)
app.register_blueprint(bookings_bp)
app.register_blueprint(users_bp)

@app.route("/", methods=["GET"])
def home():
    """
    Root endpoint for health check.

    Purpose:
        Confirms that the EZStay backend server is running.

    Returns:
        JSON: A success message indicating API is live.
    """

    
    return jsonify({"message": "EZStay Backend API is running!"})


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(debug=True, host="0.0.0.0", port=port)

    
