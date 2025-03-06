from flask import Flask, jsonify
from flask_cors import CORS
from routes.rooms import rooms_bp
from routes.bookings import bookings_bp
from routes.users import users_bp

app = Flask(__name__)
CORS(app)

# Register Routes
app.register_blueprint(rooms_bp)
app.register_blueprint(bookings_bp)
app.register_blueprint(users_bp)

@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "EZStay Backend API is running!"})

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)
    