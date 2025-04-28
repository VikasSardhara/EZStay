
package com.example.homepage.ROOM;
public class Room {
    private int bookingId;
    private int roomId;
    private String size;
    private String type;

    public Room(int roomId, String size, String type) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.size = size;
        this.type = type;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getSize() {
        return size;
    }

    public String getType() {
        return type;
    }
}
