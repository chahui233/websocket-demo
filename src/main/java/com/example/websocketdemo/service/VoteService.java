package com.example.websocketdemo.service;
import com.example.websocketdemo.mapper.PollMapper;
import com.example.websocketdemo.mapper.PollOptionMapper;
import com.example.websocketdemo.mapper.UserMapper;
import com.example.websocketdemo.mapper.VoteRecordMapper;
import com.example.websocketdemo.model.Poll;
import com.example.websocketdemo.model.PollOption;
import com.example.websocketdemo.model.User;
import com.example.websocketdemo.model.VoteRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VoteService {


        @Autowired
        private PollMapper pollMapper;

        @Autowired
        private PollOptionMapper pollOptionMapper;

    @Autowired
    private VoteRecordMapper voteRecordMapper;

        @Autowired
        private UserMapper userMapper;

        private final Map<String, Integer> onlineByRoom = new ConcurrentHashMap<>();

    @Transactional
    public String createRoom(String username, String title, List<String> options) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        String roomId = generateRoomId();
        while (pollMapper.findByRoomId(roomId) != null) {
            roomId = generateRoomId();
        }

        Poll poll = new Poll();
        poll.setRoomId(roomId);
        poll.setTitle(title);
        poll.setDescription("自定义投票");
        poll.setVoting(false);
        poll.setCreatorUserId(user.getId());
        poll.setDeleted(false);
        pollMapper.insertPoll(poll);

        int no = 1;
        for (String item : options) {
            PollOption option = new PollOption();
            option.setPollId(poll.getId());
            option.setOptionNo(no++);
            option.setContent(item);
            option.setVoteCount(0);
            pollOptionMapper.insertOption(option);
        }
        return roomId;
    }

    @Transactional
    public boolean vote(String roomId, String username, int optionNo) {

        Poll poll = pollMapper.findActiveByRoomId(roomId);
        if (poll == null || !Boolean.TRUE.equals(poll.getVoting())) {
                return false;
            }

        User user = userMapper.findByUsername(username);
        if (user == null) {
            return false;
        }

            PollOption option = pollOptionMapper.findByPollIdAndOptionNo(poll.getId(), optionNo);
            if (option == null) {
                return false;
            }

            VoteRecord existing = voteRecordMapper.findByPollIdAndUserId(poll.getId(), user.getId());
            if (existing != null) {
                return false;
            }

            VoteRecord voteRecord = new VoteRecord();
            voteRecord.setPollId(poll.getId());
            voteRecord.setOptionId(option.getId());
            voteRecord.setUserId(user.getId());
            voteRecordMapper.insertVote(voteRecord);

            pollOptionMapper.incrementVoteCount(option.getId());

            return true;
        }

            @Transactional
            public boolean startVote(String roomId, String username, String role) {

                Poll poll = pollMapper.findActiveByRoomId(roomId);
                if (poll == null) {
                    return false;
                }

                if (!canManagePoll(poll, username, role)) {
                    return false;
                }

                pollMapper.updateVotingStatus(poll.getId(), true);
                pollMapper.updateStartTime(poll.getId());

                pollOptionMapper.resetVoteCountByPollId(poll.getId());
                voteRecordMapper.deleteByPollId(poll.getId());
                return true;
            }

                @Transactional
                public boolean stopVote(String roomId, String username, String role) {

                    Poll poll = pollMapper.findActiveByRoomId(roomId);
                    if (poll == null) {
                        return false;
                    }

                    if (!canManagePoll(poll, username, role)) {
                        return false;
                    }

                    pollMapper.updateVotingStatus(poll.getId(), false);
                    pollMapper.updateEndTime(poll.getId());
                    pollMapper.markDeleted(poll.getId());
                    onlineByRoom.remove(roomId);
                    return true;
                }

                    public Map<Integer, Integer> getResult(String roomId) {

                        Poll poll = pollMapper.findByRoomId(roomId);
                        if (poll == null) {
                            return Collections.emptyMap();
                        }
                        List<PollOption> options = pollOptionMapper.findByPollId(poll.getId());

                    Map<Integer, Integer> result = new HashMap<>();

                    for (PollOption option : options) {
                        result.put(option.getOptionNo(), option.getVoteCount());
                    }

                        return result;
                    }

                    public boolean isVoting(String roomId) {

                        Poll poll = pollMapper.findByRoomId(roomId);
                        return poll != null && Boolean.TRUE.equals(poll.getVoting());
                    }

    public boolean roomExistsActive(String roomId) {
        return pollMapper.findActiveByRoomId(roomId) != null;
    }

    public Map<String, Object> getActiveRoomDetail(String roomId) {
        Poll poll = pollMapper.findActiveByRoomId(roomId);
        if (poll == null) {
            return null;
        }

        List<PollOption> options = pollOptionMapper.findByPollId(poll.getId());
        options.sort(Comparator.comparing(PollOption::getOptionNo));

        List<Map<String, Object>> optionData = new ArrayList<>();
        for (PollOption option : options) {
            optionData.add(Map.of(
                    "optionNo", option.getOptionNo(),
                    "content", option.getContent()
            ));
        }

        return Map.of(
                "roomId", poll.getRoomId(),
                "title", poll.getTitle(),
                "options", optionData
        );
    }

    public List<Map<String, Object>> getMyHistory(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return Collections.emptyList();
        }

        List<Poll> polls = pollMapper.findDeletedByCreatorUserId(user.getId());
        List<Map<String, Object>> history = new ArrayList<>();

        for (Poll poll : polls) {
            List<PollOption> options = pollOptionMapper.findByPollId(poll.getId());
            options.sort(Comparator.comparing(PollOption::getOptionNo));

            List<Map<String, Object>> result = new ArrayList<>();
            for (PollOption option : options) {
                result.add(Map.of(
                        "optionNo", option.getOptionNo(),
                        "content", option.getContent(),
                        "voteCount", option.getVoteCount()
                ));
            }

            history.add(Map.of(
                    "roomId", poll.getRoomId(),
                    "title", poll.getTitle(),
                    "endTime", poll.getEndTime(),
                    "result", result
            ));
        }

        return history;
                    }

                        public void addUser(String roomId) {
                            onlineByRoom.merge(roomId, 1, Integer::sum);
                        }

                        public void removeUser(String roomId) {
                            onlineByRoom.compute(roomId, (key, count) -> {
                                if (count == null || count <= 1) {
                                    return null;
                                }
                                return count - 1;
                            });
                        }

                        public int getOnline(String roomId) {
                            return onlineByRoom.getOrDefault(roomId, 0);
                        }

    private boolean canManagePoll(Poll poll, String username, String role) {
        if ("admin".equals(role)) {
            return true;
                        }

        User user = userMapper.findByUsername(username);
        return user != null && Objects.equals(poll.getCreatorUserId(), user.getId());
    }

    private String generateRoomId() {
        int value = 100000 + new Random().nextInt(900000);
        return String.valueOf(value);
                    }
                }