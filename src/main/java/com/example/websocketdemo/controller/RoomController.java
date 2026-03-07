package com.example.websocketdemo.controller;

import com.example.websocketdemo.model.User;
import com.example.websocketdemo.service.VoteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private VoteService voteService;

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> request,
                                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
        }

        String title = request.getOrDefault("title", "").trim();
        String rawOptions = request.getOrDefault("options", "");
        List<String> options = Arrays.stream(rawOptions.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (title.isEmpty()) {
            return ResponseEntity.badRequest().body("标题不能为空");
        }

        if (options.size() < 2) {
            return ResponseEntity.badRequest().body("至少需要2个投票选项");
        }

        String roomId = voteService.createRoom(user.getUsername(), title, options);
        return ResponseEntity.ok(Map.of("roomId", roomId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getActiveRoom(@PathVariable String roomId,
                                           HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
        }

        Map<String, Object> roomDetail = voteService.getActiveRoomDetail(roomId);
        if (roomDetail == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not-found");
        }

        return ResponseEntity.ok(roomDetail);
    }

    @GetMapping("/my-history")
    public ResponseEntity<?> myHistory(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
        }

        return ResponseEntity.ok(voteService.getMyHistory(user.getUsername()));
    }
}
