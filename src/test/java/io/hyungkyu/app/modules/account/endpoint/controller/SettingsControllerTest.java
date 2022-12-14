package io.hyungkyu.app.modules.account.endpoint.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyungkyu.app.infra.IntegrationTest;
import io.hyungkyu.app.modules.account.WithAccount;
import io.hyungkyu.app.modules.account.application.AccountService;
import io.hyungkyu.app.modules.account.domain.entity.Account;
import io.hyungkyu.app.modules.zone.domain.entity.Zone;
import io.hyungkyu.app.modules.account.endpoint.controller.form.TagForm;
import io.hyungkyu.app.modules.account.endpoint.controller.form.ZoneForm;
import io.hyungkyu.app.modules.account.infra.repository.AccountRepository;
import io.hyungkyu.app.modules.tag.domain.entity.Tag;
import io.hyungkyu.app.modules.tag.infra.repository.TagRepository;
import io.hyungkyu.app.modules.zone.infra.repository.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired ZoneRepository zoneRepository;
    @Autowired AccountService accountService;
    @Autowired TagRepository tagRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @Test
    @DisplayName("????????? ??????: ????????? ??????")
    @WithAccount("gudrb")
    void updateProfile() throws Exception {
        String bio = "??? ??? ??????";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));
        Account gudrb = accountRepository.findByNickname("gudrb");
        assertEquals(bio, gudrb.getProfile().getBio());
    }

    @Test
    @DisplayName("????????? ??????: ????????? ??????")
    @WithAccount("gudrb")
    void updateProfileWithError() throws Exception {
        String bio = "35??? ???????????????35??? ???????????????35??? ???????????????35??? ???????????????";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
        Account gudrb = accountRepository.findByNickname("gudrb");
        assertNull(gudrb.getProfile().getBio());
    }

    @Test
    @DisplayName("????????? ??????")
    @WithAccount("gudrb")
    void updateProfileForm() throws Exception {
        String bio = "??? ??? ??????";
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @Test
    @DisplayName("???????????? ?????? ???")
    @WithAccount("gudrb")
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @Test
    @DisplayName("???????????? ??????: ????????? ??????")
    @WithAccount("gudrb")
    void updatePassword() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .param("newPassword", "12341234")
                        .param("newPasswordConfirm", "12341234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));
        Account account = accountRepository.findByNickname("gudrb");
        assertTrue(passwordEncoder.matches("12341234", account.getPassword()));
    }

    @Test
    @DisplayName("???????????? ??????: ????????? ??????(?????????)")
    @WithAccount("gudrb")
    void updatePasswordWithNotMatchedError() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .param("newPassword", "12341234")
                        .param("newPasswordConfirm", "12121212")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @DisplayName("???????????? ??????: ????????? ??????(??????)")
    @WithAccount("gudrb")
    void updatePasswordWithLengthError() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .param("newPassword", "1234")
                        .param("newPasswordConfirm", "1234")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @DisplayName("?????? ?????? ?????? ???")
    @WithAccount("gudrb")
    void updateNotificationForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_NOTIFICATION_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_NOTIFICATION_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notificationForm"));
    }

    @Test
    @DisplayName("?????? ?????? ??????: ????????? ??????")
    @WithAccount("gudrb")
    void updateNotification() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_NOTIFICATION_URL)
                        .param("studyCreatedByEmail", "true")
                        .param("studyCreatedByWeb", "true")
                        .param("studyRegistrationResultByEmail", "true")
                        .param("studyRegistrationResultByWeb", "true")
                        .param("studyUpdatedByEmail", "true")
                        .param("studyUpdatedByWeb", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_NOTIFICATION_URL))
                .andExpect(flash().attributeExists("message"));
        Account account = accountRepository.findByNickname("gudrb");
        assertTrue(account.getNotificationSetting().isStudyCreatedByEmail());
        assertTrue(account.getNotificationSetting().isStudyCreatedByWeb());
        assertTrue(account.getNotificationSetting().isStudyRegistrationResultByEmail());
        assertTrue(account.getNotificationSetting().isStudyRegistrationResultByWeb());
        assertTrue(account.getNotificationSetting().isStudyUpdatedByEmail());
        assertTrue(account.getNotificationSetting().isStudyUpdatedByWeb());
    }

    @Test
    @DisplayName("????????? ?????? ???")
    @WithAccount("gudrb")
    void updateNicknameForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @DisplayName("????????? ??????: ????????? ??????")
    @WithAccount("gudrb")
    void updateNickname() throws Exception {
        String newNickname = "gudrb2";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));
        Account account = accountRepository.findByNickname(newNickname);
        assertEquals(newNickname, account.getNickname());
    }

    @Test
    @DisplayName("????????? ??????: ????????? ??????(??????)")
    @WithAccount("gudrb")
    void updateNicknameWithShortNickname() throws Exception {
        String newNickname = "g";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @DisplayName("????????? ??????: ????????? ??????(??????)")
    @WithAccount("gudrb")
    void updateNicknameWithDuplicatedNickname() throws Exception {
        String newNickname = "gudrb";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @DisplayName("?????? ?????? ???")
    @WithAccount("gudrb")
    void updateTagForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @Test
    @DisplayName("?????? ??????")
    @WithAccount("gudrb")
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        String tagTitle = "newTag";
        tagForm.setTagTitle(tagTitle);
        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        Tag tag = tagRepository.findByTitle(tagTitle).orElse(null);
        assertNotNull(tag);
        assertTrue(accountRepository.findByNickname("gudrb").getTags().contains(tag));
    }

    @Test
    @DisplayName("?????? ??????")
    @WithAccount("gudrb")
    void removeTag() throws Exception {
        Account gudrb = accountRepository.findByNickname("gudrb");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());

        accountService.addTag(gudrb, newTag);
        assertTrue(gudrb.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        String tagTitle = "newTag";
        tagForm.setTagTitle(tagTitle);
        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertFalse(gudrb.getTags().contains(newTag));
    }

    @Test
    @DisplayName("????????? ?????? ?????? ?????? ???")
    @WithAccount("gudrb")
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ZONE_URL))
                .andExpect(view().name(SettingsController.SETTINGS_ZONE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @Test
    @DisplayName("????????? ?????? ?????? ??????")
    @WithAccount("gudrb")
    void addZone() throws Exception {
        Zone testZone = Zone.builder().city("test").localNameOfCity("????????????").province("????????????").build();
        zoneRepository.save(testZone);
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());
        mockMvc.perform(post(SettingsController.SETTINGS_ZONE_URL + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        Account account = accountRepository.findByNickname("gudrb");
        assertTrue(account.getZones().contains(testZone));
    }

    @Test
    @DisplayName("????????? ?????? ?????? ??????")
    @WithAccount("gudrb")
    void removeZone() throws Exception {
        Account gudrb = accountRepository.findByNickname("gudrb");
        Zone testZone = Zone.builder().city("test").localNameOfCity("????????????").province("????????????").build();
        zoneRepository.save(testZone);
        accountService.addZone(gudrb, testZone);
        assertTrue(gudrb.getZones().contains(testZone));
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());
        mockMvc.perform(post(SettingsController.SETTINGS_ZONE_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertFalse(gudrb.getZones().contains(testZone));
    }
}