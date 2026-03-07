package com.example.websocketdemo.mapper;

import com.example.websocketdemo.model.Poll;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PollMapper {

    Poll findByRoomId(@Param("roomId") String roomId);

    void insertPoll(Poll poll);

    void updateVotingStatus(@Param("pollId") Long pollId,
                            @Param("voting") boolean voting);

    void updateStartTime(@Param("pollId") Long pollId);

    void updateEndTime(@Param("pollId") Long pollId);
}
