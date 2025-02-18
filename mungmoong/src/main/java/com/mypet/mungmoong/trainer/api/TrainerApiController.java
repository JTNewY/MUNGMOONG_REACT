package com.mypet.mungmoong.trainer.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.mypet.mungmoong.main.model.Event;
import com.mypet.mungmoong.orders.dto.Orders;
import com.mypet.mungmoong.orders.service.OrdersService;
import com.mypet.mungmoong.pet.dto.Pet;
import com.mypet.mungmoong.pet.service.PetService;
import com.mypet.mungmoong.trainer.dto.Career;
import com.mypet.mungmoong.trainer.dto.Certificate;
import com.mypet.mungmoong.trainer.dto.Files;
import com.mypet.mungmoong.trainer.dto.Schedule;
import com.mypet.mungmoong.trainer.dto.Trainer;
import com.mypet.mungmoong.trainer.service.CareerService;
import com.mypet.mungmoong.trainer.service.CertificateService;
import com.mypet.mungmoong.trainer.service.FileService;
import com.mypet.mungmoong.trainer.service.ScheduleService;
import com.mypet.mungmoong.trainer.service.TrainerService;
import com.mypet.mungmoong.users.dto.Users;
import com.mypet.mungmoong.users.service.UsersService;

import lombok.extern.slf4j.Slf4j;

/**
 * [GET] - /trainer/list : 훈련사 목록 화면
 * [GET] - /trainer/read : 훈련사 조회 화면
 * [GET] - /trainer/insert : 훈련사 등록 화면
 * [POST] - /trainer/insert : 훈련사 등록 처리
 * [GET] - /trainer/update : 훈련사 수정 화면
 * [POST] - /trainer/update : 훈련사 수정 처리
 * [POST] - /trainer/delete : 훈련사 삭제 처리
 */
@Slf4j
@Controller
@RequestMapping("/api/trainer")
public class TrainerApiController {

    private Logger logger = LoggerFactory.getLogger(TrainerApiController.class);

    @GetMapping("/{page}")
    public String test(@PathVariable("page") String page) {
        return "/trainer/" + page;
    }

    // ⭐ 데이터 요청과 화면 출력
    // Controller --> Service (데이터 요청)
    // Controller <-- Service (데이터 전달)
    // Controller --> Model (모델 등록)
    // View <-- Model (데이터 출력)
    @Autowired
    private TrainerService trainerService;

    @Autowired
    private FileService fileService;

    @Autowired
    private CareerService careerService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private PetService petService;

    @Autowired
    private UsersService userService;

    // orders 목록
    @GetMapping("/orders")
    public ResponseEntity<?> ordersList(HttpSession session) throws Exception {
        log.info("[GET] - /api/orders");
        Integer trainerNo = (Integer) session.getAttribute("trainerNo");
        if (trainerNo == null) {
            log.error("트레이너 번호를 세션에서 찾을 수 없습니다.");
            // 트레이너 번호가 없을 경우 에러 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("트레이너 번호를 세션에서 찾을 수 없습니다.");
        }

        // 데이터 요청
        log.info("trainerNo : " + trainerNo);
        List<Orders> ordersList = ordersService.listByTrainer(trainerNo);

        // 데이터와 함께 상태 코드 반환
        return ResponseEntity.ok(ordersList);
    }

    
    // 입금 내역 목록
    @GetMapping("/deposit")
    public ResponseEntity<?> deposit(HttpSession session) throws Exception {
        log.info("[GET] - /api/deposit");
        Integer trainerNo = (Integer) session.getAttribute("trainerNo");
        if (trainerNo == null) {
            log.error("트레이너 번호를 세션에서 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("트레이너 번호를 세션에서 찾을 수 없습니다.");
        }

        // 데이터 요청
        log.info("trainerNo : " + trainerNo);
        List<Orders> ordersList = ordersService.listByTrainer(trainerNo);

        // 총 금액 계산
        int totalAmount = ordersList.stream().mapToInt(Orders::getPrice).sum();

        // 승인된 주문 필터링 및 총 금액 계산
        List<Orders> approvedOrdersList = ordersList.stream()
                .filter(order -> "approval".equals(order.getStatus()))
                .collect(Collectors.toList());

        int totalApprovedAmount = approvedOrdersList.stream().mapToInt(Orders::getPrice).sum();

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("ordersList", ordersList);
        response.put("totalAmount", totalAmount);
        response.put("approvedOrdersList", approvedOrdersList);
        response.put("totalApprovedAmount", totalApprovedAmount);

        // JSON 형식으로 데이터 반환
        return ResponseEntity.ok(response);
    }

    
    // Meaning 수정 작업
    @PostMapping("/orders")
    public ResponseEntity<?> updateOrderMeaning(@RequestParam("orderNo") int orderNo, @RequestParam("meaning") int meaning)
            throws Exception {
        ordersService.updateMeaning(orderNo, meaning);
        return ResponseEntity.ok("Order meaning updated successfully.");
    }
    

    // Orders 조회
    @GetMapping("/orders_details")
    public ResponseEntity<?> ordersDetails(@RequestParam("no") int no) throws Exception {
        Orders orders = ordersService.select(no);
        int petNo = orders.getPetNo();
        log.info("petNo :  " + petNo);
        Pet pet = petService.findPetById(petNo);
        log.info(":::::  pet  ::::::" + pet.toString());
        log.info(":::: orders :::::" + orders.toString());
    
        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orders);
        response.put("pet", pet);
    
        // JSON 형식으로 데이터 반환
        return ResponseEntity.ok(response);
    }
    

    // 훈련사 정보 조회 (경력, 소개, 자격증)
    @GetMapping("/info")
    public ResponseEntity<?> select(@RequestParam("userId") String userId) throws Exception {
        Trainer trainer = trainerService.select(userId);
        List<Career> careerList = careerService.select(userId);
        List<Certificate> certificateList = certificateService.listByUserId(userId);
    
        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("trainer", trainer);
        response.put("careerList", careerList);
        response.put("certificateList", certificateList);
    
        // JSON 형식으로 데이터 반환
        return ResponseEntity.ok(response);
    }
    

    // 훈련사 정보 등록
    @PostMapping("/join_data")
    public ResponseEntity<?> insertPro(@RequestBody Trainer trainer, HttpSession session) {
        try {
            Users user = (Users) session.getAttribute("user");

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in.");
            }

            trainer.setUserId(user.getUserId());
            trainer.setCareerList(trainer.toCareerList());
            trainer.setCertificateList(trainer.toCertificateList());
            log.info("trainer 로그조회 : " + trainer);

            int result = trainerService.insert(trainer);

            if (result > 0) {
                String userId = (String) session.getAttribute("userId");
                Users updatedUser = userService.select(userId);
                session.setAttribute("user", updatedUser);
                return ResponseEntity.ok("Trainer data inserted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert trainer data.");
            }
        } catch (Exception e) {
            log.error("Error occurred while processing trainer data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while processing trainer data: " + e.getMessage());
        }
    }


    
    // 훈련사 수정 화면
    @GetMapping("/info_update")
    public ResponseEntity<?> update(@RequestParam("userId") String userId, HttpSession session) throws Exception {
        Trainer trainer = trainerService.select(userId);
        List<Career> careerList = careerService.select(userId); // select -> listByUserId
        List<Certificate> certificateList = certificateService.listByUserId(userId);
        Files file = new Files(); // assuming a default constructor or appropriate method to get Files object
        List<Files> fileList = fileService.listByParent(file);
        Integer trainerNo = (Integer) session.getAttribute("trainerNo");

        if (trainerNo == null) {
            log.error("트레이너 번호를 세션에서 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("트레이너 번호를 세션에서 찾을 수 없습니다.");
        }

        log.info("--------------------------------------------------------------");
        log.info(careerList.toString());

        file.setParentTable("trainer");
        file.setParentTable("certificate");

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("trainer", trainer);
        response.put("trainerNo", trainerNo);
        response.put("careerList", careerList);
        response.put("certificateList", certificateList);
        response.put("fileList", fileList);

        // JSON 형식으로 데이터 반환
        return ResponseEntity.ok(response);
    }


    // 훈련사 수정 처리
    @PostMapping("/info_update")
    public ResponseEntity<?> updatePro(@RequestBody Trainer trainer, @RequestParam("files") List<MultipartFile> files, HttpSession session) throws Exception {
        log.info(":::::::::::::::::: 훈련사 정보 수정 :::::::::::::::::::");
        log.info("trainser : " + trainer.toString());
    
        Integer trainerNo = (Integer) session.getAttribute("trainerNo");
        if (trainerNo == null) {
            log.error("트레이너 번호를 세션에서 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("트레이너 번호를 세션에서 찾을 수 없습니다.");
        }
        log.info("세션에서 가져온 트레이너 번호 : " + trainerNo);
    
        List<Career> careerList = trainer.toCareerList();
        for (Career career : careerList) {
            career.setTrainerNo(trainerNo);
            int result = (career.getNo() > 0) ? careerService.update(career) : careerService.insert(career);
            log.info(result > 0 ? "성공!" : "실패..");
        }
    
        List<Certificate> certificateList = trainer.toCertificateList();
        log.info("certificateList : " + certificateList);
        log.info("업로드 파일 목록 - files : " + files);
    
        for (int i = 0; i < certificateList.size(); i++) {
            Certificate certificate = certificateList.get(i);
            certificate.setTrainerNo(trainerNo);
    
            int result = (certificate.getNo() > 0) ? certificateService.update(certificate) : certificateService.insert(certificate);
            if (result > 0) {
                log.info("자격증 성공");
            } else {
                log.info("자격증 실패");
            }
    
            if (i < files.size()) {
                MultipartFile file = files.get(i);
                if (!file.isEmpty()) {
                    Files fileEntity = new Files();
                    fileEntity.setFile(file);
                    fileEntity.setParentTable("certificate");
                    fileEntity.setParentNo(certificate.getNo());  // 이 시점에서 certificate.getNo()는 올바른 값이어야 함
                    fileService.upload(fileEntity);
                    certificate.setImgFile(fileEntity); // Files 객체를 자격증 객체에 설정
                    certificate.insertImg();
                }
            }
        }
    
        int result = trainerService.update(trainer);
        log.debug("Trainer data : {}", trainer);
    
        if (result > 0) {
            return ResponseEntity.ok("Trainer information updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update trainer information.");
        }
    }
    
    
    

    // @PostMapping("/info_update")
    // public String updatePro(Trainer trainer, HttpSession session) throws
    // Exception {
    // log.info(":::::::::::::::::: 훈련사 정보 수정 :::::::::::::::::::");
    // log.info("trainser : " + trainer.toString());
    // List<Career> careerList = trainer.toCareerList();
    // List<Certificate> certificateList = trainer.toCertificateList();
    // List<Files> filesList = fileService.list();
    // List<MultipartFile> files = trainer.getFiles();

    // log.info("--------------------------------");
    // log.info(careerList.toString());
    // log.info("트레이너 번호가 뭘까요 : " + trainer.getNo());

    // Integer trainerNo = (Integer) session.getAttribute("trainerNo");
    // if (trainerNo == null) {
    // log.error("트레이너 번호를 세션에서 찾을 수 없습니다.");
    // return "redirect:/trainer/info_update?userId=" + trainer.getUserId() +
    // "&error=session";
    // }
    // log.info("세션에서 가져온 트레이너 번호 : " + trainerNo);

    // for (Career career : careerList) {
    // career.setTrainerNo(trainerNo);
    // log.info("trainerNo : " + trainerNo);

    // int result = 0;
    // if (career.getNo() > 0) {
    // result = careerService.update(career);
    // log.info("수정 완료!");
    // } else {
    // career.setTrainerNo(trainerNo);
    // result = careerService.insert(career);
    // log.info("등록 완료!");
    // }

    // if (result > 0) {
    // log.info("성공!");
    // } else {
    // log.info(career.toString());
    // log.info("실패..");
    // }
    // }

    // log.info(":::::::::::::::::::::: certificateList ::::::::::::::::::::::::");
    // log.info("certificateList : " + certificateList);
    // log.info(":::::::::::::::::::::: 업로드 파일 목록 - files
    // ::::::::::::::::::::::::");
    // log.info("files : " + files);

    // for (int i = 0; i < certificateList.size(); i++) {
    // Certificate certificate = certificateList.get(i);
    // certificate.setTrainerNo(trainerNo);
    // log.info("trainerNo : " + trainerNo);

    // // 자격증 객체에 이미지 파일 담음
    // certificate.setInsertFile(files.get(i));
    // certificate.insertImg();

    // int result = 0;
    // if (certificate.getNo() > 0) {
    // result = certificateService.update(certificate);
    // log.info(";;;;;;;;자격증 이미지update;;;;;;;;; : " + filesList.toString());
    // log.info("자격증 수정");
    // } else {
    // certificate.setTrainerNo(trainerNo);
    // result = certificateService.insert(certificate);
    // log.info(";;;;;;;;자격증 이미지insert;;;;;;;;; : " + filesList.toString());
    // log.info("자격증 등록");
    // }

    // if (result > 0) {
    // log.info("자격증 성공");
    // } else {
    // log.info(certificate.toString());
    // log.info("자격증 실패");
    // }
    // }

    // int result = trainerService.update(trainer);

    // log.debug("Trainer data : {}",trainer);

    // if(result>0)
    // {
    // return "redirect:/trainer/info_update?userId=" + trainer.getUserId();
    // }return"redirect:/trainer/info_update?userId="+trainer.getUserId()+"&error";
    // }



    // 스케쥴 👩‍🏫(full calendar 샘플)
    @GetMapping("/schedule")
    public ResponseEntity<?> scheduleCalendar(HttpSession session) throws Exception {
        Integer trainerNo = (Integer) session.getAttribute("trainerNo");
        if (trainerNo == null) {
            log.error("트레이너 번호를 세션에서 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("트레이너 번호를 세션에서 찾을 수 없습니다.");
        }
        List<Schedule> scheduleList = scheduleService.select(trainerNo);
        Map<String, Object> response = new HashMap<>();
        response.put("trainerNo", trainerNo);
        response.put("scheduleList", scheduleList);
        return ResponseEntity.ok(response);
    }


    // 스케쥴 등록
    @PostMapping("/schedule")
    public ResponseEntity<?> saveSchedule(@RequestBody Schedule schedule, HttpSession session) {
        try {
            Integer trainerNo = (Integer) session.getAttribute("trainerNo");
            Users loginUser = (Users) session.getAttribute("user");
            if (trainerNo == null || loginUser == null) {
                log.error("트레이너 번호를 세션에서 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("트레이너 번호를 세션에서 찾을 수 없습니다.");
            }
            String userId = loginUser.getUserId();
            log.info("저장된 아이디 : " + userId);
    
            schedule.setTrainerNo(trainerNo);
            schedule.setUserId(userId);
            int result = scheduleService.insert(schedule);
    
            if (result > 0) {
                log.info("스케쥴 등록이 완료되었습니다╰(*°▽°*)╯");
                return ResponseEntity.ok("Schedule saved successfully.");
            }
        } catch (Exception e) {
            log.error("Error occurred while processing trainer data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while processing trainer data: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save schedule.");
    }
    

    /**
     * 캘린더 데이터
     * - 훈련사 번호를 받아오면 해당 훈련사의 일정을
     * JSON 데이터로 응답함
     */
    @ResponseBody
    @GetMapping("/schedule/event")
    public ResponseEntity<?> trainerScheduleEvent(@RequestParam("trainerNo") int trainerNo) throws Exception {
        List<Schedule> scheduleList = scheduleService.select(trainerNo);
        List<Event> eventList = new ArrayList<>();
        for (Schedule schedule : scheduleList) {
            int no = schedule.getNo();
            String title = schedule.getTitle();
            Date date = schedule.getScheduleDate();
            String description = schedule.getContent();
            eventList.add(new Event(no, title, description, date));
        }
        return ResponseEntity.ok(eventList);
    }
    

    
    // 일정 삭제
    @DeleteMapping("/schedule/event/{no}")
    public ResponseEntity<?> deleteTrainerScheduleEvent(@PathVariable("no") int no) throws Exception {
        log.info("스케쥴 번호 - no " + no);
        int result = scheduleService.deleteByNo(no);
        if (result > 0) {
            return ResponseEntity.ok("SUCCESS");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }
    }
    

}
