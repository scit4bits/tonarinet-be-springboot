
import json
import csv
import re
from math import radians, sin, cos, sqrt, atan2

try:
    from shapely.geometry import shape
except ImportError:
    print("Shapely library not found. Please install it using 'pip install shapely'")
    exit()

def haversine_distance(lon1, lat1, lon2, lat2):
    R = 6371000  # Radius of Earth in meters
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    distance = R * c
    return distance

name_2_translations = {
    "Dobong": "도봉구", "Dongdaemun": "동대문구", "Dongjak": "동작구",
    "Eunpyeong": "은평구", "Gangbuk": "강북구", "Gangdong": "강동구",
    "Gangnam": "강남구", "Gangseo": "강서구", "Geumcheon": "금천구",
    "Guro": "구로구", "Gwanak": "관악구", "Gwangjin": "광진구",
    "Jongno": "종로구", "Jung": "중구", "Jungnang": "중랑구",
    "Mapo": "마포구", "Nowon": "노원구", "Seocho": "서초구",
    "Seodaemun": "서대문구", "Seongbuk": "성북구", "Seongdong": "성동구",
    "Songpa": "송파구", "Yangcheon": "양천구", "Yeongdeungpo": "영등포구",
    "Yongsan": "용산구"
}

name_3_translations = {
    "Ahyeon": "아현동", "Amsa1(il)": "암사1동", "Amsa2(i)": "암사2동", "Amsa3(sam)": "암사3동",
    "Anam": "안암동", "Apgujeong": "압구정동", "Balsan1": "발산1동",
    "Bangbae1(il)": "방배1동", "Bangbae2(i)": "방배2동", "Bangbae3(sam)": "방배3동", "Bangbae4(sa)": "방배4동",
    "Bangbaebon": "방배본동", "Banghak1(il)": "방학1동", "Banghak2(i)": "방학2동", "Banghak3(sam)": "방학3동",
    "Banghwa1(il)": "방화1동", "Banghwa2(i)": "방화2동", "Banghwa3(sam)": "방화3동",
    "Bangi1(il)": "방이1동", "Bangi2(i)": "방이2동", "Banpo1(il)": "반포1동", "Banpo2(i)": "반포2동",
    "Banpo3(sam)": "반포3동", "Banpo4(sa)": "반포4동", "Banpobon": "반포본동",
    "Beon1(il)": "번1동", "Beon2(i)": "번2동", "Beon3(sam)": "번3동", "Bogwang": "보광동",
    "Bomun": "보문동", "Boramae": "보라매동", "Buam": "부암동", "Bugahyeon": "북아현동",
    "Bukgajwa1(il)": "북가좌1동", "Bukgajwa2(i)": "북가좌2동", "Bulgwang1(il)": "불광1동", "Bulgwang2(i)": "불광2동",
    "Chang1(il)": "창1동", "Chang2(i)": "창2동", "Chang3(sam)": "창3동", "Chang4(sa)": "창4동", "Chang5(o)": "창5동",
    "Changsin1(il)": "창신1동", "Changsin2(i)": "창신2동", "Changsin3(sam)": "창신3동",
    "Cheongdam": "청담동", "Cheonggu": "청구동", "Cheongnim": "청림동",
    "Cheongnyangni": "청량리동", "Cheongnyong": "청룡동", "Cheongpa": "청파동",
    "Cheongunhyoja": "청운효자동", "Cheonho1(il)": "천호1동", "Cheonho2(i)": "천호2동", "Cheonho3(sam)": "천호3동",
    "Cheonyeon": "천연동", "Chunghyeon": "충현동", "Daebang": "대방동",
    "Daechi1(il)": "대치1동", "Daechi2(i)": "대치2동", "Daechi4(sa)": "대치4동",
    "Daehak": "대학동", "Daeheung": "대흥동", "Daejo": "대조동",
    "Daerim1(il)": "대림1동", "Daerim2(i)": "대림2동", "Daerim3(sam)": "대림3동",
    "Dangsan1": "당산1동", "Dangsan2": "당산2동", "Dapsimni1(il)": "답십리1동", "Dapsimni2(i)": "답십리2동",
    "Dasan": "다산동", "Deungchon1(il)": "등촌1동", "Deungchon2(i)": "등촌2동", "Deungchon3(sam)": "등촌3동",
    "Dobong1(il)": "도봉1동", "Dobong2(i)": "도봉2동", "Dogok1(il)": "도곡1동", "Dogok2(i)": "도곡2동",
    "Dohwa": "도화동", "Doksan1(il)": "독산1동", "Doksan2(i)": "독산2동", "Doksan3(sam)": "독산3동", "Doksan4(sa)": "독산4동",
    "Donam1(il)": "돈암1동", "Donam2(i)": "돈암2동", "Donghwa": "동화동", "Dongseon": "동선동",
    "Dorim": "도림동", "Dunchon1(il)": "둔촌1동", "Dunchon2(i)": "둔촌2동", "Euljiro": "을지로동",
    "Euncheon": "은천동", "Eungam1(il)": "응암1동", "Eungam2(i)": "응암2동", "Eungam3(sam)": "응암3동",
    "Eungbong": "응봉동", "Gaebong1(il)": "개봉1동", "Gaebong2(i)": "개봉2동", "Gaebong3(sam)": "개봉3동",
    "Gaepo1(il)": "개포1동", "Gaepo2(i)": "개포2동", "Gaepo4(sa)": "개포4동", "Gahoe": "가회동",
    "Galhyeon1(il)": "갈현1동", "Galhyeon2(i)": "갈현2동", "Gangil": "강일동",
    "Garak1(il)": "가락1동", "Garak2(i)": "가락2동", "Garakbon": "가락본동", "Garibong": "가리봉동",
    "Gasan": "가산동", "Gayang1(il)": "가양1동", "Gayang2(i)": "가양2동", "Gayang3(sam)": "가양3동",
    "Geoyeo1(il)": "거여1동", "Geoyeo2(i)": "거여2동", "Geumho1Ga": "금호1가동", "Geumho2·3Ga": "금호2·3가동",
    "Geumho4Ga": "금호4가동", "Gil": "길동", "Gireum1(il)": "길음1동", "Gireum2(i)": "길음2동",
    "Gocheok1(il)": "고척1동", "Gocheok2(i)": "고척2동", "Godeok1(il)": "고덕1동", "Godeok2(i)": "고덕2동",
    "Gongdeok": "공덕동", "Gonghang": "공항동", "Gongneung1(il)": "공릉1동", "Gongneung2(i)": "공릉2동",
    "Gunja": "군자동", "Guro1(il)": "구로1동", "Guro2(i)": "구로2동", "Guro3(sam)": "구로3동", "Guro4(sa)": "구로4동", "Guro5(o)": "구로5동",
    "Gusan": "구산동", "Guui1(il)": "구의1동", "Guui2(i)": "구의2동", "Guui3(sam)": "구의3동",
    "Gwanghui": "광희동", "Gwangjang": "광장동", "Gyonam": "교남동",
    "Haengdang1(il)": "행당1동", "Haengdang2(i)": "행당2동", "Haengun": "행운동",
    "Hagye1(il)": "하계1동", "Hagye2(i)": "하계2동", "Hangangno": "한강로동", "Hannam": "한남동",
    "Hapjeong": "합정동", "Heukseok": "흑석동", "Hoegi": "회기동", "Hoehyeon": "회현동",
    "Hongeun1(il)": "홍은1동", "Hongeun2(i)": "홍은2동", "Hongje1(il)": "홍제1동", "Hongje2(i)": "홍제2동", "Hongje3(sam)": "홍제3동",
    "Huam": "후암동", "Hwagok1(il)": "화곡1동", "Hwagok2(i)": "화곡2동", "Hwagok3(sam)": "화곡3동", "Hwagok4(sa)": "화곡4동",
    "Hwagok6(yuk)": "화곡6동", "Hwagok8(pal)": "화곡8동", "Hwagokbon": "화곡본동", "Hwanghak": "황학동",
    "Hwayang": "화양동", "Hwigyeong1(il)": "휘경1동", "Hwigyeong2(i)": "휘경2동", "Hyehwa": "혜화동",
    "Hyochang": "효창동", "Ichon1(il)": "이촌1동", "Ichon2(i)": "이촌2동", "Ihwa": "이화동",
    "Imun1(il)": "이문1동", "Imun2(i)": "이문2동", "Inheon": "인헌동", "Insu": "인수동",
    "Irwon1(il)": "일원1동", "Irwon2(i)": "일원2동", "Irwonbon": "일원본동", "Itaewon1(il)": "이태원1동", "Itaewon2(i)": "이태원2동",
    "Jamsil2(i)": "잠실2동", "Jamsil3(sam)": "잠실3동", "Jamsil4(sa)": "잠실4동", "Jamsil6(yuk)": "잠실6동", "Jamsil7(chil)": "잠실7동",
    "Jamsilbon": "잠실본동", "Jamwon": "잠원동", "Jangan1(il)": "장안1동", "Jangan2(i)": "장안2동",
    "Jangchung": "장충동", "Jangji": "장지동", "Jangwi1(il)": "장위1동", "Jangwi2(i)": "장위2동", "Jangwi3(sam)": "장위3동",
    "Jayang1(il)": "자양1동", "Jayang2(i)": "자양2동", "Jayang3(sam)": "자양3동", "Jayang4": "자양4동",
    "Jegi": "제기동", "Jeongneung1(il)": "정릉1동", "Jeongneung2(i)": "정릉2동", "Jeongneung3(sam)": "정릉3동", "Jeongneung4(sa)": "정릉4동",
    "Jeonnong1(il)": "전농1동", "Jeonnong2(i)": "전농2동", "Jeungsan": "증산동", "Jingwan": "진관동",
    "Jongam": "종암동", "Jongno1·2·3·4Ga": "종로1·2·3·4가동", "Jongno5·6Ga": "종로5·6가동",
    "Jowon": "조원동", "Jungang": "중앙동", "Junggok1(il)": "중곡1동", "Junggok2(i)": "중곡2동", "Junggok3(sam)": "중곡3동", "Junggok4(sa)": "중곡4동",
    "Junggye1(il)": "중계1동", "Junggye2·3": "중계2·3동", "Junggye4(sa)": "중계4동", "Junggyebon": "중계본동",
    "Junghwa1(il)": "중화1동", "Junghwa2(i)": "중화2동", "Jungnim": "중림동", "Macheon1(il)": "마천1동", "Macheon2(i)": "마천2동",
    "Majang": "마장동", "Mangu3(sam)": "망우3동", "Mangubon": "망우본동", "Mangwon1(il)": "망원1동", "Mangwon2(i)": "망원2동",
    "Mia": "미아동", "Miseong": "미성동", "Mok1(il)": "목1동", "Mok2(i)": "목2동", "Mok3(sam)": "목3동", "Mok4(sa)": "목4동", "Mok5(o)": "목5동",
    "Muak": "무악동", "Muk1(il)": "묵1동", "Muk2(i)": "묵2동", "Mullae": "문래동",
    "Munjeong1(il)": "문정1동", "Munjeong2(i)": "문정2동", "Myeong": "명동", "Myeongil1(il)": "명일1동", "Myeongil2(i)": "명일2동",
    "Myeonmok2(i)": "면목2동", "Myeonmok3·8": "면목3·8동", "Myeonmok4(sa)": "면목4동", "Myeonmok5(o)": "면목5동", "Myeonmok7(chil)": "면목7동",
    "Myeonmokbon": "면목본동", "Naegok": "내곡동", "Nakseongdae": "낙성대동",
    "Namgajwa1(il)": "남가좌1동", "Namgajwa2(i)": "남가좌2동", "Namhyeon": "남현동", "Namyeong": "남영동",
    "Nangok": "난곡동", "Nanhyang": "난향동", "Neung": "능동", "Nokbeon": "녹번동",
    "Nonhyeon1(il)": "논현1동", "Nonhyeon2(i)": "논현2동", "Noryangjin1(il)": "노량진1동", "Noryangjin2(i)": "노량진2동",
    "Ogeum": "오금동", "Oksu": "옥수동", "Oryu1(il)": "오류1동", "Oryu2(i)": "오류2동", "Oryun": "오륜동",
    "Pil": "필동", "Pungnap1(il)": "풍납1동", "Pungnap2(i)": "풍납2동", "Pyeongchang": "평창동",
    "Sadang1(il)": "사당1동", "Sadang2(i)": "사당2동", "Sadang3(sam)": "사당3동", "Sadang4(sa)": "사당4동", "Sadang5(o)": "사당5동",
    "Sageun": "사근동", "Sajik": "사직동", "Samcheong": "삼청동", "Samgaksan": "삼각산동",
    "Samjeon": "삼전동", "Samseon": "삼선동", "Samseong": "삼성동", "Samseong1(il)": "삼성1동", "Samseong2(i)": "삼성2동",
    "Samyang": "삼양동", "Sangam": "상암동", "Sangbong1(il)": "상봉1동", "Sangbong2(i)": "상봉2동",
    "Sangdo1(il)": "상도1동", "Sangdo2(i)": "상도2동", "Sangdo3(sam)": "상도3동", "Sangdo4(sa)": "상도4동",
    "Sanggye1(il)": "상계1동", "Sanggye10(sip)": "상계10동", "Sanggye2(i)": "상계2동", "Sanggye3·4": "상계3·4동",
    "Sanggye5(o)": "상계5동", "Sanggye6·7": "상계6·7동", "Sanggye8(pal)": "상계8동", "Sanggye9(gu)": "상계9동",
    "Sangil": "상일동", "Segok": "세곡동", "Seobinggo": "서빙고동", "Seocho1(il)": "서초1동", "Seocho2(i)": "서초2동", "Seocho3(sam)": "서초3동", "Seocho4(sa)": "서초4동",
    "Seogang": "서강동", "Seogyo": "서교동", "Seokchon": "석촌동", "Seokgwan": "석관동",
    "Seongbuk": "성북동", "Seonghyeon": "성현동", "Seongnae1(il)": "성내1동", "Seongnae2(i)": "성내2동", "Seongnae3(sam)": "성내3동",
    "Seongsan1(il)": "성산1동", "Seongsan2(i)": "성산2동", "Seongsu1(il)-ga1(il)": "성수1가1동", "Seongsu1(il)-ga2(i)": "성수1가2동",
    "Seongsu2(i)-ga1(il)": "성수2가1동", "Seongsu2(i)-ga3(sam)": "성수2가3동",
    "Seorim": "서림동", "Seowon": "서원동", "Siheung1(il)": "시흥1동", "Siheung2(i)": "시흥2동", "Siheung3(sam)": "시흥3동", "Siheung4(sa)": "시흥4동", "Siheung5(o)": "시흥5동",
    "Sillim": "신림동", "Sinchon": "신촌동", "Sindaebang1(il)": "신대방1동", "Sindaebang2(i)": "신대방2동",
    "Sindang": "신당동", "Sindang5(o)": "신당5동", "Sindorim": "신도림동", "Singil1(il)": "신길1동", "Singil3(sam)": "신길3동",
    "Singil4(sa)": "신길4동", "Singil5(o)": "신길5동", "Singil6(yuk)": "신길6동", "Singil7(chil)": "신길7동",
    "Sinjeong1(il)": "신정1동", "Sinjeong2(i)": "신정2동", "Sinjeong3(sam)": "신정3동", "Sinjeong4(sa)": "신정4동", "Sinjeong6(yuk)": "신정6동", "Sinjeong7(chil)": "신정7동",
    "Sinnae1(il)": "신내1동", "Sinnae2(i)": "신내2동", "Sinsa": "신사동", "Sinsa1(il)": "신사1동", "Sinsa2(i)": "신사2동",
    "Sinsu": "신수동", "Sinwol1(il)": "신월1동", "Sinwol2(i)": "신월2동", "Sinwol3(sam)": "신월3동", "Sinwol4(sa)": "신월4동", "Sinwol5(o)": "신월5동", "Sinwol6(yuk)": "신월6동", "Sinwol7(chil)": "신월7동",
    "Sinwon": "신원동", "Sogong": "소공동", "Songcheon": "송천동", "Songjeong": "송정동",
    "Songjung": "송중동", "Songpa1(il)": "송파1동", "Songpa2(i)": "송파2동", "Ssangmun1(il)": "쌍문1동", "Ssangmun2(i)": "쌍문2동", "Ssangmun3(sam)": "쌍문3동", "Ssangmun4(sa)": "쌍문4동",
    "Sugung": "수궁동", "Sungin1(il)": "숭인1동", "Sungin2(i)": "숭인2동", "Susaek": "수색동",
    "Suseo": "수서동", "Suyu1(il)": "수유1동", "Suyu2(i)": "수유2동", "Suyu3(sam)": "수유3동",
    "Ui": "우이동", "Ujangsan": "우장산동", "Wangsimni2": "왕십리2동", "Wangsimnidoseon": "왕십리도선동",
    "Wirye": "위례동", "Wolgok1(il)": "월곡1동", "Wolgok2(i)": "월곡2동", "Wolgye1(il)": "월계1동", "Wolgye2(i)": "월계2동", "Wolgye3(sam)": "월계3동",
    "Wonhyoro1": "원효로1동", "Wonhyoro2": "원효로2동", "Yaksu": "약수동", "Yangjae1(il)": "양재1동", "Yangjae2(i)": "양재2동",
    "Yangpyeong1": "양평1동", "Yangpyeong2": "양평2동", "Yeokchon": "역촌동", "Yeoksam1(il)": "역삼1동", "Yeoksam2(i)": "역삼2동",
    "Yeomchang": "염창동", "Yeomni": "염리동", "Yeongdeungpo": "영등포동", "Yeongdeungpobon": "영등포본동",
    "Yeonhui": "연희동", "Yeonnam": "연남동", "Yeoui": "여의동", "Yongdap": "용답동",
    "Yonggang": "용강동", "Yongmun": "용문동", "Yongsan2Ga": "용산2가동", "Yongsin": "용신동"
}

def translate_name_3(name_3):
    if name_3 in name_3_translations:
        return name_3_translations[name_3]
    return name_3

def main():
    with open(r'\Documents\gemini_workspace\gadm41_KOR_3.json', 'r', encoding='utf-8') as f:
        data = json.load(f)

    seoul_features = [feature for feature in data['features'] if feature['properties']['NAME_1'] == 'Seoul']
    data['features'] = seoul_features

    simplified_features = []
    for feature in data['features']:
        simplified_properties = {
            'GID_0': feature['properties']['GID_0'],
            'NL_NAME_1': feature['properties']['NL_NAME_1'],
            'NAME_2': feature['properties']['NAME_2'],
            'NAME_3': feature['properties']['NAME_3']
        }
        
        geom = shape(feature['geometry'])
        centroid = geom.centroid
        min_lon, min_lat, max_lon, max_lat = geom.bounds
        radius1 = haversine_distance(centroid.x, centroid.y, min_lon, min_lat)
        radius2 = haversine_distance(centroid.x, centroid.y, max_lon, max_lat)
        max_radius = max(radius1, radius2)
        
        simplified_feature = {
            'type': 'Feature',
            'properties': simplified_properties,
            'geometry': {
                'latitude': centroid.y,
                'longitude': centroid.x,
                'radius': int(max_radius)
            }
        }
        simplified_features.append(simplified_feature)
    
    data['features'] = simplified_features

    ratio = 0.5
    for feature in data['features']:
        if 'radius' in feature['geometry']:
            feature['geometry']['radius'] *= ratio
            feature['geometry']['radius'] = int(feature['geometry']['radius'])

    for feature in data['features']:
        properties = feature['properties']
        if 'NAME_2' in properties and properties['NAME_2'] in name_2_translations:
            properties['NAME_2'] = name_2_translations[properties['NAME_2']]
        
        if 'NAME_3' in properties:
            properties['NAME_3'] = translate_name_3(properties['NAME_3'])

    with open(r'seoul_regions_final.csv', 'w', newline='', encoding='utf-8') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(['id', 'country_code', 'category1', 'category2', 'category3', 'category4', 'longitude', 'latitude', 'radius'])
        
        for feature in data['features']:
            properties = feature['properties']
            geometry = feature['geometry']
            
            csv_writer.writerow([
                '',
                'kor',
                '서울특별시',
                '서울특별시',
                properties['NAME_2'],
                properties['NAME_3'],
                geometry['longitude'],
                geometry['latitude'],
                geometry['radius']
            ])

    print("Successfully processed the data and created 'seoul_regions_final.csv'")

if __name__ == '__main__':
    main()
