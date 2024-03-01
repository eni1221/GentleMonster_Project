package order.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import common.controller.AbstractController;
import member.domain.MemberVO;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import order.domain.OrderVO;
import order.model.OrderDAO;
import order.model.OrderDAO_imple;

public class OrderStatusUpdateAction extends AbstractController {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
		HttpSession session = req.getSession();

		if (session.getAttribute("loginUser") != null
				&& ((MemberVO) session.getAttribute("loginUser")).getGrade() == 2) {
			// 로그인 및 관리자등급일 경우

			if ("POST".equalsIgnoreCase(req.getMethod())) {
				// POST로 들어왔을 경우 = 정상적으로 들어왔을 경우

				OrderDAO odao = new OrderDAO_imple();

				Map<String, String> paraMap = new HashMap<>();
				paraMap.put("orderId", req.getParameter("orderId"));
				paraMap.put("orderStatus", req.getParameter("orderStatus"));
				int num = odao.updateOrderStatus(paraMap);
				if (num == 1) {
					// 주문현황이 변경되었을 경우 변경된 주문현황 정보 가져오기
					
					OrderVO ovo = odao.getOrderOne(paraMap);
					
					if("3".equals(ovo.getOrderStatus()) || "4".equals(ovo.getOrderStatus())) {
						// 배송중 혹은 배송 완료의 경우
						String api_key = ""; // 보안 상 삭제
						String api_secret = ""; // 보안 상 삭제
						Message coolsms = new Message(api_key, api_secret);

						// == 4개 파라미터(to, from, type, text)는 필수사항이다. ==
						HashMap<String, String> paraMapToSMS = new HashMap<>();
						paraMapToSMS.put("to", ovo.getTel()); // 수신번호
						paraMapToSMS.put("from", ""); // 발신번호 _ 보안 상 삭제
						paraMapToSMS.put("type", "LMS"); // Message type ( SMS(단문), LMS(장문), MMS, ATA )
						
						String smsData = "[GENTLE MONSTER] " + ovo.getFullName() + "님 께서 주문하신 주문번호 #" + paraMap.get("orderId") + "는 현재 CJ대한통운 택배로 ";
						if("3".equals(ovo.getOrderStatus())) {
							// 배송중일 경우
							smsData += "배송중입니다.";
						}else {
							smsData += "배송 완료했습니다.";
						}
						
						paraMapToSMS.put("text", smsData); // 문자내용
						paraMapToSMS.put("app_version", "JAVA SDK v2.2"); // application name and version

						try {
							coolsms.send(paraMapToSMS);
							// org.json.simple.JSONObject 이어야 한다.

						} catch (CoolsmsException e) {
							e.printStackTrace();
						}
					}

					super.setRedirect(true);
					super.setViewPage(req.getContextPath() + "/admin/orderList.gm");

				} else {
					String message = "주문현황을 수정하는 데에 문제가 생겼습니다. 원래 화면으로 돌아갑니다.";
					String loc = "javascript:history.back()";
					req.setAttribute("message", message);
					req.setAttribute("loc", loc);

					super.setRedirect(false);
					super.setViewPage("/jsp/common/msg.jsp");
				}
			} else {
				// GET으로 들어왔을 경우 = 잘못된 경로로 들어왔을 경우

				String message = "잘못된 경로입니다. 인덱스 화면으로 이동합니다.";
				String loc = req.getContextPath() + "/index.gm";

				req.setAttribute("message", message);
				req.setAttribute("loc", loc);

				super.setRedirect(false);
				super.setViewPage("/jsp/common/msg.jsp");
			}

		} else {
			// 로그인하지 않았을 경우

			String message = "권한이 없습니다. 관리자 로그인 후 이용해주세요. 인덱스 화면으로 이동합니다.";
			String loc = req.getContextPath() + "/index.gm";

			req.setAttribute("message", message);
			req.setAttribute("loc", loc);

			super.setRedirect(false);
			super.setViewPage("/jsp/common/msg.jsp");

		} // end of if (session.getAttribute("loginUser") != null)
			// -----------------------------------------------------
	}

}
