package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.TicketCategory;
import capstone.capstone_shop.domain.TicketStatus;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.*;
import capstone.capstone_shop.repository.UserRepository;
import capstone.capstone_shop.service.SupportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/support")
public class SupportController {

    private final SupportService supportService;
    private final UserRepository userRepository;

    private LoginUserDto getLoginUser(HttpSession session) {
        return (LoginUserDto) session.getAttribute("loginUser");
    }

    private boolean isAdmin(LoginUserDto dto) {
        return dto != null && dto.userRole() == UserRole.ADMIN;
    }

    /** 목록 (관리자: 검색/필터 전체 조회, 사용자: 내 티켓) */
    @GetMapping
    public String list(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        LoginUserDto loginUser = getLoginUser(session);
        boolean admin = isAdmin(loginUser);
        model.addAttribute("isAdmin", admin);

        PageRequest pageable = PageRequest.of(page, size);
        Page<TicketSummaryDto> tickets;

        if (admin) {
            tickets = supportService.searchForAdmin(
                    new TicketSearchCond(status, category, keyword),
                    pageable
            );
            model.addAttribute("status", status);
            model.addAttribute("category", category);
            model.addAttribute("keyword", keyword);
        } else {
            if (loginUser == null) return "redirect:/login?redirectURL=/support";
            tickets = supportService.getMyTickets(loginUser.id(), pageable);
        }

        model.addAttribute("tickets", tickets);
        return "support/list";
    }

    /** 작성 폼 */
    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        LoginUserDto loginUser = getLoginUser(session);
        if (loginUser == null) return "redirect:/login?redirectURL=/support/new";

        model.addAttribute("form", new TicketCreateRequest("", null, "", false, null));
        model.addAttribute("categories", TicketCategory.values());
        return "support/new";
    }

    /** 작성 처리 */
    @PostMapping("/new")
    public String create(
            HttpSession session,
            @Valid @ModelAttribute("form") TicketCreateRequest form,
            BindingResult binding,
            RedirectAttributes ra,
            Model model
    ) {
        LoginUserDto loginUser = getLoginUser(session);
        if (loginUser == null) return "redirect:/login?redirectURL=/support/new";

        if (binding.hasErrors()) {
            model.addAttribute("categories", TicketCategory.values());
            return "support/new";
        }

        User user = userRepository.findById(loginUser.id())
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));

        Long id = supportService.createTicket(user, form);
        ra.addFlashAttribute("toast", "문의가 등록되었습니다.");
        return "redirect:/support/" + id;
    }

    /** 상세 (관리자는 전체, 사용자는 본인 것만) */
    @GetMapping("/{id}")
    public String detail(
            HttpSession session,
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra
    ) {
        LoginUserDto loginUser = getLoginUser(session);
        boolean admin = isAdmin(loginUser);
        model.addAttribute("isAdmin", admin);

        try {
            TicketDetailDto dto;
            if (admin) {
                dto = supportService.getAdminTicketDetail(id);
            } else {
                if (loginUser == null) return "redirect:/login?redirectURL=/support/" + id;
                dto = supportService.getMyTicketDetail(loginUser.id(), id);
            }
            model.addAttribute("ticket", dto);
            return "support/detail";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/support";
        }
    }

    /** (관리자) 목록 */
    @GetMapping("/admin")
    public String adminList(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        LoginUserDto loginUser = getLoginUser(session);
        if (!isAdmin(loginUser)) return "redirect:/login?redirectURL=/support/admin";

        PageRequest pageable = PageRequest.of(page, size);
        Page<TicketSummaryDto> tickets = supportService.searchForAdmin(
                new TicketSearchCond(status, category, keyword),
                pageable
        );

        model.addAttribute("isAdmin", true);
        model.addAttribute("tickets", tickets);
        model.addAttribute("status", status);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        return "support/admin_list";
    }

    /** (관리자) 상세 */
    @GetMapping("/admin/{id}")
    public String adminDetail(
            HttpSession session,
            @PathVariable Long id,
            Model model,
            RedirectAttributes ra
    ) {
        LoginUserDto loginUser = getLoginUser(session);
        if (!isAdmin(loginUser)) return "redirect:/login?redirectURL=/support/admin/" + id;

        try {
            TicketDetailDto dto = supportService.getAdminTicketDetail(id);
            model.addAttribute("isAdmin", true);
            model.addAttribute("ticket", dto);
            return "support/admin_detail";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/support/admin";
        }
    }

    /** (관리자) 답글 등록 */
    @PostMapping("/admin/{id}/reply")
    public String reply(
            HttpSession session,
            @PathVariable Long id,
            @RequestParam String content,
            RedirectAttributes ra
    ) {
        LoginUserDto loginUser = getLoginUser(session);
        if (!isAdmin(loginUser)) return "redirect:/login?redirectURL=/support/admin/" + id;

        User admin = userRepository.findById(loginUser.id())
                .orElseThrow(() -> new IllegalStateException("관리자 정보를 찾을 수 없습니다."));

        Long replyId = supportService.addReplyAsAdmin(admin, id, new ReplyCreateRequest(content));
        ra.addFlashAttribute("toast", "답변이 등록되었습니다. (#" + replyId + ")");
        return "redirect:/support/admin/" + id;
    }

    /** (관리자) 상태 변경 */
    @PostMapping("/admin/{id}/status")
    public String changeStatus(
            HttpSession session,
            @PathVariable Long id,
            @RequestParam TicketStatus status,
            RedirectAttributes ra
    ) {
        LoginUserDto loginUser = getLoginUser(session);
        if (!isAdmin(loginUser)) return "redirect:/login?redirectURL=/support/admin/" + id;

        supportService.changeStatus(id, status);
        ra.addFlashAttribute("toast", "상태가 변경되었습니다.");
        return "redirect:/support/admin/" + id;
    }
}
