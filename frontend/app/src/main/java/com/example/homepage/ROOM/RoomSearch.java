package com.example.homepage.ROOM;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RoomSearch {
    // Updated method to work with assets folder
    public static void RoomSearch(Context context, String fileName, int searchRoomId, RoomSearchCallback callback) {
        AssetManager assetManager = context.getAssets();

        Log.d("RoomSearch", "Opening file: " + fileName);
        Log.d("roomid","hi" + searchRoomId);


        try (InputStream is = assetManager.open(fileName);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            // Skip the header (first line)
            br.readLine();

            // Read each line of the CSV file
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");  //example line: 101(room id),King,Non-Smoking
                Log.d("RoomSearch", "reading line: " + line);

                int roomID = Integer.parseInt(data[0]); //ex: gets 101
                Log.d("RoomSearch", "reading line: " + data[0]);


                if (roomID == searchRoomId) {
                    Room room = new Room(roomID, data[1], data[2]);
                    callback.onRoomFound(room);
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface RoomSearchCallback {
        void onRoomFound(Room room);
    }
}
