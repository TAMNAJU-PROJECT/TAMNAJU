const map = document.getElementById('map'); // <div id="map></div>

map.init = function(latitude, longitude) {
    if(latitude === undefined || longitude === undefined) {
        navigator.geolocation.getCurrentPosition(function(e) {
            // latitude = e.coords.latitude;
            // longitude = e.coords.longitude;
            map.init(e.coords.latitude, e.coords.longitude);
        }, function() {
            map.init(35.8715411, 128.601505);
        });
        return;
    }
    // getCurrentPosition 은 논 블러킹(Non blocking)


    const option = {
        center: new kakao.maps.LatLng(latitude, longitude),
        level: 3 //확대, 축소 레벨
    };
    map.instance = new kakao.maps.Map(map, option);

    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if(xhr.status >= 200 && xhr.status < 300) {
            const responseObject = JSON.parse(xhr.responseText);
            const items = responseObject['data'];
            const geocoder = new kakao.maps.services.Geocoder();
            for (const item of items) {
                geocoder.addressSearch(item['nx'], function(result, status){
                    if(status !== kakao.maps.services.Status.OK) {
                        return;
                    }
                    const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                    const marker = new kakao.maps.Marker({
                        map: map.instance,
                        position: coords
                    });
                    kakao.maps.event.addListener(marker, 'click', function() {
                        cover.show();
                        info.show(item);
                    });
                });
            }

        } else {
            alert('맛집 데이터를 불러오지 못하였습니다. \n\n 확인을 누르면 페이지를 새로 고칩니다.');
            location.reload();
        }
    };
    xhr.open('GET', './XHR3.json');
    xhr.setRequestHeader('Access-Control-Allow-Origin', 'https://www.daegufood.go.kr');
    xhr.send();
};

map.init();

const cover = document.getElementById('cover');

cover.show = () => cover.classList.add('visible');
cover.hide = () => cover.classList.remove('visible');
cover.isVisible = () => cover.classList.contains('visible');

const info = document.getElementById('info');

info.querySelector('[rel="close"]').onclick = function() {
    cover.hide();
    info.hide();
}

info.show = (item) => {
    info.querySelector('[rel="temperature"]').innerText = item['TMP'];
    info.querySelector('[rel="sky"]').innerText = item['SKY'];
    info.querySelector('[rel="rain-probability"]').innerText = item['POP'];
    info.querySelector('[rel="rain-style"]').innerText = item['PTY'];


    info.classList.add('visible');
};
info.hide = () => info.classList.remove('visible');
info.isVisible = () => info.classList.contains('visible');