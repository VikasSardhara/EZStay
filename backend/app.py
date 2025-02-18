from flask import Flask, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # Enable CORS for frontend communication

@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "EZStay Backend API is running!"})

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)
