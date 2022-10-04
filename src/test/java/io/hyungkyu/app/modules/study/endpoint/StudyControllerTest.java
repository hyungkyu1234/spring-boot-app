package io.hyungkyu.app.modules.study.endpoint;

import io.hyungkyu.app.infra.IntegrationTest;
import io.hyungkyu.app.modules.account.WithAccount;
import io.hyungkyu.app.modules.account.domain.entity.Account;
import io.hyungkyu.app.modules.account.infra.repository.AccountRepository;
import io.hyungkyu.app.modules.study.application.StudyService;
import io.hyungkyu.app.modules.study.domain.entity.Study;
import io.hyungkyu.app.modules.study.endpoint.form.StudyForm;
import io.hyungkyu.app.modules.study.infra.repository.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class StudyControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyRepository studyRepository;
    @Autowired StudyService studyService;

    @Test
    @DisplayName("스터디 폼 조회")
    @WithAccount("gudrb")
    void studyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @DisplayName("스터디 추가: 정상")
    @WithAccount("gudrb")
    void createStudy() throws Exception {
        String studyPath = "study-test";
        mockMvc.perform(post("/new-study")
                        .param("path", studyPath)
                        .param("title", "study-title")
                        .param("shortDescription", "short-description")
                        .param("fullDescription", "fullDescription")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + studyPath));
        assertTrue(studyRepository.existsByPath(studyPath));
    }

    @Test
    @DisplayName("스터디 추가: 입력값 비정상")
    @WithAccount("gudrb")
    void createStudyWithError() throws Exception {
        String studyPath = "s";
        mockMvc.perform(post("/new-study")
                        .param("path", studyPath)
                        .param("title", "study-title")
                        .param("shortDescription", "short-description")
                        .param("fullDescription", "fullDescription")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("스터디 추가: 입력값 중복")
    @WithAccount("gudrb")
    void createStudyWithDuplicate() throws Exception {
        Account account = accountRepository.findByNickname("gudrb");
        String duplicatedPath = "study-path";
        studyService.createNewStudy(StudyForm.builder()
                .path(duplicatedPath)
                .title("study-title")
                .shortDescription("short-description")
                .fullDescription("full-description")
                .build(), account);
        mockMvc.perform(post("/new-study")
                        .param("path", duplicatedPath)
                        .param("title", "study-title")
                        .param("shortDescription", "short-description")
                        .param("fullDescription", "fullDescription")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("스터디 뷰")
    @WithAccount("gudrb")
    void studyView() throws Exception {
        Account account = accountRepository.findByNickname("gudrb");
        String studyPath = "study-path";
        studyService.createNewStudy(StudyForm.builder()
                .path(studyPath)
                .title("study-title")
                .shortDescription("short-description")
                .fullDescription("full-description")
                .build(), account);
        mockMvc.perform(get("/study/" + studyPath))
                .andExpect(status().isOk())
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @DisplayName("스터디 멤버 뷰")
    @WithAccount("gudrb")
    void studyMemberView() throws Exception {
        Account account = accountRepository.findByNickname("gudrb");
        String studyPath = "study-path";
        studyService.createNewStudy(StudyForm.builder()
                .path(studyPath)
                .title("study-title")
                .shortDescription("short-description")
                .fullDescription("full-description")
                .build(), account);
        mockMvc.perform(get("/study/" + studyPath + "/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/members"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @DisplayName("스터디 가입")
    @WithAccount(value = {"gudrb", "test"})
    void joinStudy() throws Exception {
        Account manager = accountRepository.findByNickname("gudrb");
        String studyPath = "study-path";
        Study study = studyService.createNewStudy(StudyForm.builder()
                .path(studyPath)
                .title("study-title")
                .shortDescription("short-description")
                .fullDescription("full-description")
                .build(), manager);
        mockMvc.perform(get("/study/" + studyPath + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + studyPath + "/members"));
        Account member = accountRepository.findByNickname("test");
        assertTrue(study.getMembers().contains(member));
    }

    @Test
    @DisplayName("스터디 탈퇴")
    @WithAccount(value = {"gudrb", "test"})
    void leaveStudy() throws Exception {
        Account manager = accountRepository.findByNickname("gudrb");
        String studyPath = "study-path";
        Study study = studyService.createNewStudy(StudyForm.builder()
                .path(studyPath)
                .title("study-title")
                .shortDescription("short-description")
                .fullDescription("full-description")
                .build(), manager);
        Account member = accountRepository.findByNickname("test");
        studyService.addMember(study, member);
        mockMvc.perform(get("/study/" + studyPath + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + studyPath + "/members"));
        assertFalse(study.getMembers().contains(member));
    }
}