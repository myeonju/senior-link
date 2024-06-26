document.addEventListener('DOMContentLoaded', function () {
    var selectedStart, selectedEnd, calendar;

    $(function () {
        $.ajax({
            url: "/schedule/list/" + serviceId,
            method: "GET",
            dataType: "json",
            success: function (data) {
                console.log("서버 응답 데이터:", data);

                var calendarEl = document.getElementById('calendar');
                calendar = new FullCalendar.Calendar(calendarEl, {
                    headerToolbar: {
                        left: 'prev',
                        center: 'title',
                        right: 'next'
                    },
                    initialView: 'dayGridMonth',
                    selectable: true,
                    dayMaxEvents: true,
                    eventTimeFormat: { // 시간 표시 형식 설정
                        hour: '2-digit',
                        minute: '2-digit',
                        meridiem: false // 24시간 형식으로 설정
                    },

                    select: function (arg) {
                        selectedStart = arg.start;
                        selectedEnd = arg.end;
                        generateTimeButtons(arg.start);
                        $("#event-info").show(); // 날짜를 클릭했을 때만 이벤트 정보 표시
                    },
                    unselect: function () {
                        $("#event-info").hide(); // 날짜 선택 해제시 이벤트 정보 숨김
                    },
                    events: data
                });

                calendar.render();
            },
            fail: function (jqXHR, textStatus) {
                alert("Request failed: " + textStatus);
            }
        });
    });

    function generateTimeButtons(startDate) {
        var buttonsHtml = '';
        var events = calendar.getEvents();

        for (var i = 0; i < 24; i++) {
            var time = new Date(startDate);
            time.setHours(i, 0, 0, 0);

            var isDisabled = events.some(function (event) {
                var eventStart = new Date(event.start);
                var eventEnd = new Date(event.end);
                return time >= eventStart && time < eventEnd;
            });

            buttonsHtml += '<button class="time-button' + (isDisabled ? ' disabled' : '') + '" data-time="' + time.toISOString() + '" ' + (isDisabled ? 'disabled' : '') + '>' + i + ':00</button>';
        }
        $("#time-buttons").html(buttonsHtml);

        $(".time-button").click(function () {
            if ($(this).hasClass('disabled')) return;

            $(".time-button").removeClass("selected");
            $(this).addClass("selected");

            var time = new Date($(this).data("time"));
            selectedStart = new Date(time);
            selectedEnd = new Date(selectedStart.getTime() + 3600000);

            $("#event-info").show();
        });
    }

    $("#submit-event").click(function () {
        var requestMsg = $("#event-title").val();
        if (requestMsg && selectedStart && selectedEnd) {
            var startISOString = selectedStart.getFullYear() + '-' +
                ('0' + (selectedStart.getMonth() + 1)).slice(-2) + '-' +
                ('0' + selectedStart.getDate()).slice(-2) + ' ' +
                ('0' + selectedStart.getHours()).slice(-2) + ':' +
                ('0' + selectedStart.getMinutes()).slice(-2) + ':' +
                ('0' + selectedStart.getSeconds()).slice(-2);

            var endISOString = selectedEnd.getFullYear() + '-' +
                ('0' + (selectedEnd.getMonth() + 1)).slice(-2) + '-' +
                ('0' + selectedEnd.getDate()).slice(-2) + ' ' +
                ('0' + selectedEnd.getHours()).slice(-2) + ':' +
                ('0' + selectedEnd.getMinutes()).slice(-2) + ':' +
                ('0' + selectedEnd.getSeconds()).slice(-2);

            var eventData = {
                title: requestMsg,
                start: startISOString,
                end: endISOString,
                serviceId: serviceId
            };

            $.ajax({
                url: "/schedule/add/" + serviceId,
                method: "POST",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader(headerName, token);
                },
                contentType: 'application/json',
                data: JSON.stringify([eventData]),
                success: function (response) {
                    if (response.isReservation) {
                        alert('예약 신청이 성공적으로 되었습니다.');
                        calendar.addEvent(eventData); // 캘린더에 이벤트 추가
                        calendar.unselect(); // 선택 해제
                    } else {
                        alert(response.message);
                    }
                },
                error: function (error) {
                    console.error('Error adding event:', error);
                    alert('예약 신청에 실패했습니다.');
                }
            });
        } else {
            alert('시간과 요청사항을 입력해주세요.');
        }
    });
});
