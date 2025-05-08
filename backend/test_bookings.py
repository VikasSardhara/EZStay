import pytest
from app import app

@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

def test_home(client):
    response = client.get('/')
    assert response.status_code == 200
    assert b"EZStay Backend API is running!" in response.data

def test_book_room_success(client):
    payload = {
        "user_id": "1",
        "room_id": "101",  # make sure this exists in rooms.csv
        "check_in_date": "2025-05-10",
        "check_out_date": "2025-05-12",
        "num_guests": 2
    }
    response = client.post('/book', json=payload)
    print("Response JSON:", response.json)  # Helpful for debugging
    assert response.status_code == 201  # ✅ expect 201 Created now
    assert b"booking successful" in response.data.lower()  # ✅ match your success message

def test_book_room_missing_field(client):
    payload = {
        "user_id": "1",
        "check_in_date": "2025-05-10",
        "check_out_date": "2025-05-12",
        "num_guests": 2
    }
    response = client.post('/book', json=payload)
    print("Response JSON (missing field):", response.json)  # Debug line
    assert response.status_code == 400  # ✅ explicitly test for bad request
    assert b"missing required fields" in response.data.lower()  # ✅ check error message
