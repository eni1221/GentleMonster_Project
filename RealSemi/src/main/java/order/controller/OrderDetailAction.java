package order.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import common.controller.AbstractController;
import member.domain.MemberVO;
import order.domain.OrderVO;
import order.model.OrderDAO;
import order.model.OrderDAO_imple;

public class OrderDetailAction extends AbstractController {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
		// 테스트용 코드 시작

		MemberVO loginUser = new MemberVO();

		loginUser.setEmail("hi@naver.com");
		loginUser.setMemberId(1234);

		HttpSession session = req.getSession();

		session.setAttribute("loginUser", loginUser);
		// 테스트용 코드 끝

		if (session.getAttribute("loginUser") != null) {
			// 로그인 했을 경우

			if ("POST".equalsIgnoreCase(req.getMethod())) {
				// POST로 들어온 경우 = 정상적으로 들어온 경우

				OrderDAO odao = new OrderDAO_imple();

				Map<String, String> paraMap = new HashMap<>();
				paraMap.put("memberId", String.valueOf(((MemberVO) session.getAttribute("loginUser")).getMemberId()));
				paraMap.put("orderId", req.getParameter("orderId"));

				req.setAttribute("orderOne", odao.getPersonalOrderOne(paraMap));
				req.setAttribute("orderDetailList", odao.getPersonalOrderDetail(paraMap));

				super.setRedirect(false);
				super.setViewPage("/jsp/order/orderDetail.jsp");

			} else {
				// GET으로 들어온 경우 = 잘못된 경로로 들어온 경우

				String message = "잘못된 경로입니다. 인덱스 화면으로 이동합니다.";
				String loc = req.getContextPath() + "/index.gm";

				req.setAttribute("message", message);
				req.setAttribute("loc", loc);

				super.setRedirect(false);
				super.setViewPage("/jsp/common/msg.jsp");
			}

		} else {
			// 로그인한 유저가 아닐 경우
			String message = "주문은 로그인 한 후 이용해주세요. 로그인 화면으로 이동합니다.";
			// 수정필
			String loc = req.getContextPath() + "/login/login.gm";

			req.setAttribute("message", message);
			req.setAttribute("loc", loc);

			super.setRedirect(false);
			super.setViewPage("/jsp/common/msg.jsp");
		}

	}

}