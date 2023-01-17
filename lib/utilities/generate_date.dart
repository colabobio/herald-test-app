import 'package:intl/intl.dart';

//* Date format in android studio java: "yyyy-MM-dd HH:mm:ss"
//* Herald uses UTC time for dates

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
