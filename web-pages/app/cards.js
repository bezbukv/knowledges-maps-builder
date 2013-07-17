// Подключает карточки
// <span class="text-contents">Display the matched elements with a sliding motion.</span>

// TODO(zaqwes): Как скрыть пространство имен.

// /app/cards.js

goog.provide('cards');  // Export?

goog.require('goog.array');

var AConstants = (function () {
  var items = ['word', 'content', 'translate'];
  return {
    ITEMS : items,   // Какие есть подкарты
    CONTENT : items[1],
    TOTAL_FRONT_CARDS : items.length,

    // Selectors
    UP_TUNER_SEL : 'div.tuner-base.tuner-base-up',
    DOWN_TUNER_SEL : 'div.tuner-base.tuner-base-down',
    SUB_CARDS_SEL : ".pack-card-container > div.active-card",
    PACK_CARDS_SEL : '.pack-card-container',
    CARD_CONTAINER_SEL : '.one-card-container',
    INNER_CARDS_CONTAINER: '.pack-card-container-inner'
  }
})();

function tick(seed, sel, count, obj) {
      $(obj).parent().find(sel).each(function (key, value) {
                  var newPos = (seed+key)%count;
                  $(this).css("z-index", newPos);
                });
       //obj = null;
    }

//var Cards = (function)
function init() {
  // Данные которые будут приходить с веб-сервера
  var response = []
  var dataOneCard = {};
  dataOneCard[AConstants.CONTENT] = ["Hello Display the matched.", "Display the matched elements with a sliding motion."];
  response.push(dataOneCard);
  var dataTwoCard = {};
  dataTwoCard[AConstants.CONTENT] = ["Hello"];//, "Display."];
  response.push(dataTwoCard);

  processResponse(response);

  pureInit()
}

function pureInit() {
  $(AConstants.CARD_CONTAINER_SEL).each(function(key, value) {
     // TODO(zaqwes): У каждого тюнера есть состояние. Хорошо бы его инкапсулировать.
     // Подключаем тюнер-вверх для подкарт - нужно вынести отсюда. Это чистая инициализация.
     // TODO(zaqwes): Тюнеры конфликтуют и глючат
     // TODO(zaqwes): Может тюрены ограничить, чтобы не сбивать с толку при замыкании
     // TODO(zaqwes: TOTH: А вообще, понятно ли какая подкарты перед нами?
     // TODO(zaqwes): DANGER: Быстро утекает память! Или медленно идет сборка мусора?
     // TODO(zaqwes): использовать find() наверное не очень хорошо.
     var seedState = 0;
     $(this).find(AConstants.UP_TUNER_SEL)
       .click(function() {
           var COUNT = AConstants.TOTAL_FRONT_CARDS;
           seedState = (seedState+1)%COUNT;
           tick(seedState, AConstants.SUB_CARDS_SEL, COUNT, this);});
     $(this).find(AConstants.DOWN_TUNER_SEL)
       .click(function() {
           var COUNT = AConstants.TOTAL_FRONT_CARDS;
           seedState = (seedState-1+COUNT)%COUNT;
           tick(seedState, AConstants.SUB_CARDS_SEL, COUNT, this);});
  });
}


function processResponse(response) {
    // TODO(zaqwes): TOTH: Что будет менятся от запроса к запросу?

    // Обрабатываем все доступные карты
    $(AConstants.CARD_CONTAINER_SEL).each(function(key, value) {

        // Создаем подкарты. Так проще будет их подключить, т.к. будут дескрипторы.
        var subCardsPkg = $(this).find(AConstants.PACK_CARDS_SEL);
        subCardsPkg.empty();  // TODO(zaqwes): Не утекают ли обработчики тюнеров?
        var cardsHandles = {};

        $.each((goog.array.clone(AConstants.ITEMS)).reverse(), function (key, value) {
          cardsHandles[value] = $("<div/>").addClass('active-card').appendTo(subCardsPkg);
          $(cardsHandles[value]).css("z-index", key)
        });

        // Добавляем контент
        var handler = cardsHandles[AConstants.CONTENT];
        var content = response[key][AConstants.CONTENT];
        var countItems = content.length;
        for (var i= 0; i < countItems; ++i) {
          // Текст нужно обернуть получше
          $("<span/>").addClass("text-contents").append(content[i]).appendTo(
            $("<div/>").addClass("content-stack").appendTo(
              $("<div/>").addClass('pack-card-container-inner').appendTo(handler)));
         }

        // Добавляем тюнеры только если более одного элемента.
        if (countItems > 1) {
            var seed = 0;
            $("<div/>").addClass("tuner-base tuner-base-up-inner")
               .click(function() {
                     seed = (seed+1)%countItems;
                     tick(seed, AConstants.INNER_CARDS_CONTAINER, countItems, this);})
                .appendTo(handler);
            $("<div/>").addClass("tuner-base tuner-base-down-inner")
                .click(function() {
                  seed = (seed-1+countItems)%countItems;
                  tick(seed, AConstants.INNER_CARDS_CONTAINER, countItems, this);})
                .appendTo(handler);
        }
        handler = null;
    });  // .each()
}
