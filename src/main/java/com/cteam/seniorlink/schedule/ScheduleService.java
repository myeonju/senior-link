package com.cteam.seniorlink.schedule;

import com.cteam.seniorlink.user.UserEntity;
import com.cteam.seniorlink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    // 추가, 수정
    public ScheduleDto save(ScheduleDto scheduleDto) {
        ScheduleEntity s = scheduleRepository.save(new ScheduleEntity().toEntity(scheduleDto));
        return new ScheduleDto().toDto(s);
    }

    // pk로 검색
    public ScheduleDto getSchedule(long scheduleId) {
        ScheduleEntity s = scheduleRepository.findById(scheduleId).orElse(null);
        return new ScheduleDto().toDto(s);
    }

    //serviceId로 검색
    public ArrayList<ScheduleDto> getByServiceId(long serviceId) {
        List<ScheduleEntity> list = scheduleRepository.findByServiceId(serviceId);
        ArrayList<ScheduleDto> list2 = new ArrayList<>();
        for (ScheduleEntity s : list) {
            list2.add(new ScheduleDto().toDto(s));
        }
        return list2;
    }

    //login user로 검색
    public List<ScheduleDto> getByUser(UserEntity carereceiver) {
        List<ScheduleEntity> list = scheduleRepository.findByCarereceiver(carereceiver);
        List<ScheduleDto> list2 = list.stream()
                .map(ScheduleDto::toDto)
                .collect(Collectors.toList());

        return list2;
    }

    // 전체 리스트
    public ArrayList<ScheduleDto> getAll() {
        List<ScheduleEntity> list = scheduleRepository.findAll();
        ArrayList<ScheduleDto> list2 = new ArrayList<>();
        for (ScheduleEntity s : list) {
            list2.add(new ScheduleDto().toDto(s));
        }

        return list2;
    }

    // 삭제
    public void del(long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }
}