package com.example.websocketdemo.mapper;

import com.example.websocketdemo.model.Poll;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface PollMapper {

    Poll findByRoomId(@Param("roomId") String roomId);
    Poll findActiveByRoomId(@Param("roomId") String roomId);

    List<Poll> findDeletedByCreatorUserId(@Param("creatorUserId") Integer creatorUserId);

    void insertPoll(Poll poll);

    void updateVotingStatus(@Param("pollId") Long pollId,
                            @Param("voting") boolean voting);

    void updateStartTime(@Param("pollId") Long pollId);

    void updateEndTime(@Param("pollId") Long pollId);
    void markDeleted(@Param("pollId") Long pollId);
}
