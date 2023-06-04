package com.example.apoorpoor_backend.service;

import com.example.apoorpoor_backend.dto.*;
import com.example.apoorpoor_backend.dto.beggar.BeggarCustomRequestDto;
import com.example.apoorpoor_backend.dto.beggar.BeggarExpUpRequestDto;
import com.example.apoorpoor_backend.dto.beggar.BeggarExpUpResponseDto;
import com.example.apoorpoor_backend.dto.beggar.BeggarRequestDto;
import com.example.apoorpoor_backend.dto.beggar.BeggarResponseDto;
import com.example.apoorpoor_backend.model.*;
import com.example.apoorpoor_backend.model.enumType.BadgeType;
import com.example.apoorpoor_backend.model.enumType.ExpType;
import com.example.apoorpoor_backend.model.enumType.ItemListEnum;
import com.example.apoorpoor_backend.model.enumType.LevelType;
import com.example.apoorpoor_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class BeggarService {

    private final BeggarRepository beggarRepository;
    private final UserRepository userRepository;
    private final GetBadgeRepository getBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final ItemRepository itemRepository;

    public ResponseEntity<StatusResponseDto> createBeggar(BeggarRequestDto beggarRequestDto, String username) {
        User findUser = userCheck(username);

        Optional<Beggar> findBeggar = beggarRepository.findByUsername(username);
        if(findBeggar.isPresent())
            return new ResponseEntity<>(new StatusResponseDto("이미 푸어가 존재합니다."), HttpStatus.BAD_REQUEST);

        beggarRepository.save(new Beggar(beggarRequestDto, findUser));
        return new ResponseEntity<>(new StatusResponseDto("푸어가 생성되었어요..."), HttpStatus.OK );
    }

    public ResponseEntity<BeggarResponseDto> findBeggar(String username) {
        Beggar beggar = beggarCheck(username);
        return new ResponseEntity<>(BeggarResponseDto.of(beggar), HttpStatus.OK);
    }

    public ResponseEntity<BeggarResponseDto> updateBeggar(BeggarRequestDto beggarRequestDto, String username) {
        Beggar beggar = beggarCheck(username);
        beggar.update(beggarRequestDto);
        return new ResponseEntity<>(BeggarResponseDto.of(beggar), HttpStatus.OK);
    }

    public ResponseEntity<BeggarExpUpResponseDto> updateExp(BeggarExpUpRequestDto beggarExpUpRequestDto, String username) {
        Beggar beggar = beggarCheck(username);
        String nickname = beggar.getNickname();
        Long exp = beggar.getExp() + beggarExpUpRequestDto.getExpType().getAmount();
        Long point = beggar.getPoint() + beggarExpUpRequestDto.getExpType().getAmount();
        Long level = beggar.getLevel();

        if(beggarExpUpRequestDto.getExpType().equals(ExpType.GET_BADGE)) {
            saveBadge(beggarExpUpRequestDto.getBadgeType(), beggar);
        }

        if (LevelType.getNextExpByLevel(level) <= exp) {
            level ++;
        }

        BeggarExpUpResponseDto beggarExpUpResponseDto = BeggarExpUpResponseDto.builder()
                .nickname(nickname)
                .exp(exp)
                .level(level)
                .point(point)
                .build();

        beggar.updateExp(beggarExpUpResponseDto);
        return new ResponseEntity<>(beggarExpUpResponseDto, HttpStatus.OK);
    }

    public User userCheck(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 유저 입니다.")
        );
    }

    public void saveBadge(BadgeType badgeType, Beggar beggar) {
        Long badgeNum = badgeType.getBadgeNum();
        String badgeTitle = badgeType.getBadgeTitle();

        boolean hasBadge = beggar.getGetBadgeList().stream()
                .map(GetBadge::getBadge)
                .anyMatch(b -> b.getBadgeNum().equals(badgeNum));

        if(!hasBadge) {
            Badge badge = new Badge(badgeNum, badgeTitle);

            badgeRepository.save(badge);

            GetBadge getBadge = new GetBadge(badge, beggar);
            badge.getGetBadgeList().add(getBadge);
            getBadgeRepository.save(getBadge);

        } else {
            throw new IllegalArgumentException("이미 뱃지를 가지고 있습니다.");
        }
    }

    public ResponseEntity<String> customBeggar(BeggarCustomRequestDto beggarCustomRequestDto, String username) {
        Beggar beggar = beggarCheck(username);
        ItemListEnum itemListEnum = beggarCustomRequestDto.getItemListEnum();

        Item findItem = itemRepository.findItemByBeggar_IdAndItemNum(beggar.getId(),itemListEnum.getItemNum())
                .orElseThrow(() -> new IllegalArgumentException("가지고 있지 않은 아이템 입니다.")
                );

        String itemType = findItem.getItemType();

        switch (itemType) {
            case "tops" :
                beggar.updateCustomTops(itemListEnum);
                break;
            case "bottoms" :
                beggar.updateCustomBottoms(itemListEnum);
                break;
            case "shoes" :
                beggar.updateCustomShoes(itemListEnum);
            case "accessories" :
                beggar.updateCustomAccessories(itemListEnum);
            default:
                System.out.println("해당 아이템은 사용 할 수 없습니다.");
        }

        return new ResponseEntity<>("착용 완료", HttpStatus.OK);
    }

    public Beggar beggarCheck(String username) {
        return beggarRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("푸어를 찾을 수 없습니다.")
        );
    }
}
