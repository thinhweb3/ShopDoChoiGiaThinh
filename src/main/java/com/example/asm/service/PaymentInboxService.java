package com.example.asm.service;

import com.example.asm.entity.DonHang;
import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentInboxService {

    private static final Pattern NUMERIC_TOKEN_PATTERN = Pattern.compile("\\d[\\d\\s.,]*\\d|\\d");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String folderName;
    private final long lookbackMinutes;
    private final int maxMessages;
    private final String expectedSender;

    public PaymentInboxService(
            @Value("${payment.gmail.host:imap.gmail.com}") String host,
            @Value("${payment.gmail.port:993}") int port,
            @Value("${payment.gmail.username:${spring.mail.username:}}") String username,
            @Value("${payment.gmail.password:${spring.mail.password:}}") String password,
            @Value("${payment.gmail.folder:INBOX}") String folderName,
            @Value("${payment.gmail.lookback-minutes:0}") long lookbackMinutes,
            @Value("${payment.gmail.lookback-hours:48}") long legacyLookbackHours,
            @Value("${payment.gmail.max-messages:30}") int maxMessages,
            @Value("${payment.gmail.expected-sender:}") String expectedSender) {
        this.host = StringUtils.hasText(host) ? host.trim() : "imap.gmail.com";
        this.port = port;
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password.trim();
        this.folderName = StringUtils.hasText(folderName) ? folderName.trim() : "INBOX";
        long resolvedLookbackMinutes = lookbackMinutes > 0 ? lookbackMinutes : legacyLookbackHours * 60;
        this.lookbackMinutes = Math.max(1, resolvedLookbackMinutes);
        this.maxMessages = Math.max(1, maxMessages);
        this.expectedSender = expectedSender == null ? "" : expectedSender.trim().toLowerCase();
    }

    public Optional<PaymentEmailMatch> findMatchingPaymentEmail(DonHang order) {
        if (order == null || order.getMaDonHang() == null || order.getTongTien() == null) {
            throw new IllegalArgumentException("Đơn hàng không hợp lệ để kiểm tra email nhận tiền.");
        }
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalStateException("Chưa cấu hình Gmail để dò email nhận tiền.");
        }

        Store store = null;
        Folder folder = null;
        try {
            Session session = Session.getInstance(buildImapProperties());
            store = session.getStore("imaps");
            store.connect(host, port, username, password);

            folder = store.getFolder(folderName);
            if (folder == null || !folder.exists()) {
                throw new IllegalStateException("Không tìm thấy folder Gmail " + folderName + " để kiểm tra thanh toán.");
            }

            folder.open(Folder.READ_ONLY);
            int totalMessages = folder.getMessageCount();
            if (totalMessages <= 0) {
                return Optional.empty();
            }

            int startIndex = Math.max(1, totalMessages - maxMessages + 1);
            Message[] recentMessages = folder.getMessages(startIndex, totalMessages);
            List<Message> orderedMessages = new ArrayList<>(Arrays.asList(recentMessages));
            orderedMessages.sort((left, right) -> compareDesc(resolveReceivedDate(left), resolveReceivedDate(right)));

            LocalDateTime notBefore = LocalDateTime.now().minusMinutes(lookbackMinutes);
            if (order.getNgayDat() != null) {
                notBefore = max(notBefore, order.getNgayDat().minusMinutes(10));
            }

            for (Message message : orderedMessages) {
                if (!matchesSender(message) || !isRecentEnough(message, notBefore)) {
                    continue;
                }

                String subject = defaultText(message.getSubject());
                String body = extractText(message);
                if (matchesPaymentEmailText(subject, body, order)) {
                    return Optional.of(new PaymentEmailMatch(
                            subject,
                            extractSender(message),
                            toLocalDateTime(resolveReceivedDate(message))));
                }
            }
            return Optional.empty();
        } catch (MessagingException | IOException e) {
            throw new IllegalStateException("Không thể đọc Gmail nhận tiền. Hãy kiểm tra IMAP, tài khoản và mật khẩu ứng dụng.", e);
        } finally {
            closeQuietly(folder);
            closeQuietly(store);
        }
    }

    boolean matchesPaymentEmailText(String subject, String body, DonHang order) {
        if (order == null || order.getMaDonHang() == null || order.getTongTien() == null) {
            return false;
        }

        String combined = defaultText(subject) + "\n" + defaultText(body);
        String normalized = normalizeAlphaNumeric(combined);
        boolean hasOrderCode = normalized.contains("dh" + order.getMaDonHang());
        boolean hasExactAmount = containsAmountToken(combined, order.getTongTien());
        return hasOrderCode && hasExactAmount;
    }

    private Properties buildImapProperties() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", String.valueOf(port));
        props.put("mail.imaps.ssl.enable", "true");
        props.put("mail.imaps.connectiontimeout", "10000");
        props.put("mail.imaps.timeout", "10000");
        props.put("mail.imaps.writetimeout", "10000");
        return props;
    }

    private boolean matchesSender(Message message) throws MessagingException {
        if (!StringUtils.hasText(expectedSender)) {
            return true;
        }
        Address[] fromAddresses = message.getFrom();
        if (fromAddresses == null || fromAddresses.length == 0) {
            return false;
        }
        for (Address address : fromAddresses) {
            String text = address == null ? "" : address.toString().toLowerCase();
            if (text.contains(expectedSender)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRecentEnough(Message message, LocalDateTime notBefore) {
        Date receivedDate = resolveReceivedDate(message);
        if (receivedDate == null || notBefore == null) {
            return true;
        }
        return !toLocalDateTime(receivedDate).isBefore(notBefore);
    }

    private Date resolveReceivedDate(Message message) {
        if (message == null) {
            return null;
        }
        try {
            Date receivedDate = message.getReceivedDate();
            return receivedDate != null ? receivedDate : message.getSentDate();
        } catch (MessagingException e) {
            return null;
        }
    }

    private String extractSender(Message message) throws MessagingException {
        Address[] fromAddresses = message.getFrom();
        if (fromAddresses == null || fromAddresses.length == 0) {
            return "không rõ người gửi";
        }
        Address firstAddress = fromAddresses[0];
        if (firstAddress instanceof InternetAddress internetAddress) {
            String personal = internetAddress.getPersonal();
            if (StringUtils.hasText(personal)) {
                return personal + " <" + internetAddress.getAddress() + ">";
            }
            return internetAddress.getAddress();
        }
        return firstAddress.toString();
    }

    private String extractText(Part part) throws MessagingException, IOException {
        if (part == null) {
            return "";
        }
        if (part.isMimeType("text/plain")) {
            return defaultText(part.getContent());
        }
        if (part.isMimeType("text/html")) {
            return htmlToText(defaultText(part.getContent()));
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                String partText = extractText(multipart.getBodyPart(i));
                if (StringUtils.hasText(partText)) {
                    if (!builder.isEmpty()) {
                        builder.append('\n');
                    }
                    builder.append(partText);
                }
            }
            return builder.toString();
        }
        if (part.isMimeType("message/rfc822")) {
            Object content = part.getContent();
            if (content instanceof Part nestedPart) {
                return extractText(nestedPart);
            }
        }
        return "";
    }

    private String htmlToText(String html) {
        return defaultText(html)
                .replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("[\\t\\x0B\\f\\r ]+", " ")
                .replaceAll("\\n{2,}", "\n")
                .trim();
    }

    private boolean containsAmountToken(String text, Long expectedAmount) {
        if (expectedAmount == null) {
            return false;
        }
        String expectedDigits = String.valueOf(expectedAmount);
        Matcher matcher = NUMERIC_TOKEN_PATTERN.matcher(defaultText(text));
        while (matcher.find()) {
            String digits = matcher.group().replaceAll("\\D", "");
            if (expectedDigits.equals(digits)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeAlphaNumeric(String value) {
        String normalized = Normalizer.normalize(defaultText(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        return normalized.replaceAll("[^a-z0-9]", "");
    }

    private String defaultText(Object value) {
        return Objects.toString(value, "");
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private int compareDesc(Date left, Date right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return right.compareTo(left);
    }

    private LocalDateTime max(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    private void closeQuietly(Folder folder) {
        if (folder == null || !folder.isOpen()) {
            return;
        }
        try {
            folder.close(false);
        } catch (MessagingException ignored) {
        }
    }

    private void closeQuietly(Store store) {
        if (store == null || !store.isConnected()) {
            return;
        }
        try {
            store.close();
        } catch (MessagingException ignored) {
        }
    }

    public record PaymentEmailMatch(String subject, String sender, LocalDateTime receivedAt) {
        public String toDisplayText() {
            String senderText = StringUtils.hasText(sender) ? sender : "Gmail đã cấu hình";
            if (receivedAt == null) {
                return "Email báo có từ " + senderText + ".";
            }
            return "Email báo có từ " + senderText + " lúc " + receivedAt.format(DISPLAY_TIME) + ".";
        }
    }
}
