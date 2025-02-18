from flask import Flask, jsonify
from flask_cors import CORS
from routes.rooms import rooms_bp


app = Flask(__name__)
CORS(app)  # Enable CORS for frontend communication

app.register_blueprint(rooms_bp)

@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "EZStay Backend API is running!"})

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)
