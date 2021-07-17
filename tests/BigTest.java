
public class BigTest {
	private static SimpleDateFormat format = new SimpleDateFormat("E (dd/MM/yyyy)");
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
	private static SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");

	public static void main(String[] args) throws Exception {
		DBInterface dbInt = new DBInterface();
		dbInt.init("schedule_mailer","mailer","mailer");
		Properties prop = System.getProperties();
		prop.put("mail.smth.host", "localhost");
		prop.put("mail.smtp.port", "25");
		Session session = Session.getInstance(prop, null);
		while (true) {
			try {
				ArrayList<String[]> ids = dbInt.getIDPairs();
				for (String[] split : ids) {
					try {
						Schedule schedule = ScheduleAPI.getSchedule(split[0]);
						Date lastUpdated = dbInt.getLastUpdated(split[0]);
						if (lastUpdated == null
								|| !schedule.getPublishTimestamp().equals(lastUpdated)) {
							if (schedule == null) {
								System.err.println("Error: *****" + split[0]);
								continue;
							}
							Message msg = new MimeMessage(session);
							msg.setFrom(new InternetAddress("****@****"));
							System.out.println("Sending email to " + split[1]);
							msg.setRecipient(Message.RecipientType.TO, new InternetAddress(split[1]));
							msg.setSubject("*****:");
							
							String firstName = schedule.getFirstName().substring(0,1).toUpperCase()+schedule.getFirstName().substring(1).toLowerCase();
							String lastName = schedule.getLastName().substring(0,1).toUpperCase()+schedule.getLastName().substring(1).toLowerCase();
							
							String message = firstName + " " + lastName + " ****!";
							int weekCount = 0;
							for (Week w : schedule.getWeeks()) {
								if (w.getShifts().length != 0) {
									weekCount++;
									weekCount += 1;
									weekCount -= 1;
									message += "\n\nWeek: " + dateformat.format(w.getStart()) + " - "
											+ dateformat.format(w.getEnd()) + ":\n";
									message += "Total Hours: " + w.getTotalHours() + "\n\n";
									for (Shift shift : w.getShifts()) {
										String day = format.format(shift.getStart());
										String startTime = timeFormat.format(shift.getStart());
										String endTime = timeFormat.format(shift.getEnd());
										String position = shift.getPosition();
										double hours = shift.getNetHours();
										message += "**** " + day + ": " + startTime + " - " + endTime + "\n";
										message += "****: " + hours + "\n";
										message += "****: " + position + "\n\n";
									}
								}
							}
							if(weekCount == 0) {
								System.err.println("****");
								continue;
							}
							msg.setText(message);

							dbInt.setLastUpdated(split[0], schedule.getPublishTimestamp());
							SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
							t.connect();
							t.sendMessage(msg, msg.getAllRecipients());
							t.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("Error in sending email");
					}
				}

				Thread.sleep(1000 * 60 * 60 * 4);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error in sending emails");
			}
		}
	}
}
