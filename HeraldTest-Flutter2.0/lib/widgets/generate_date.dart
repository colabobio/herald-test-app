import 'package:intl/intl.dart';

//date format in android stuido java: "yyyy-MM-dd HH:mm:ss"
//herald uses UTC time for dates
class GenerateDate {
  String generateDate() {
    DateTime date = DateTime.now().toUtc();
    final DateFormat formatter = DateFormat('yyyy-MM-dd HH:mm:ss');
    final String formattedDate = formatter.format(date);
    return formattedDate;
  }

  int differenceBetweenDates(DateTime from, DateTime to) {
    from = DateTime(
        from.year, from.month, from.day, from.hour, from.minute, from.second);
    to = DateTime(to.year, to.month, to.day, to.hour, to.minute, to.second);
    return (to.difference(from).inSeconds).round();
  }
}
