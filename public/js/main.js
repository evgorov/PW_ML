/* Customize backbone */
var currentUser;

// set some global defaults for Backbone ajax
Backbone.ajax = function(options){
  var sessionKey = currentUser.get('sessionKey');
  if(!sessionKey){
    setTimeout(_.bind(Backbone.ajax, this, options), 100);
    return null;
  }

  var $spinner = $('[role="loading-spinner"]').show();
  if(typeof options.data === 'undefined') options.data = {};
  if(options.type !== 'GET'){
    delete options.contentType;
    if(typeof options.data === 'string') options.data = JSON.parse(options.data);
    options.data = _.reduce(options.data, function(acc, v, k){
      if(typeof v === 'string'){
        acc[k] = v;
      } else {
        acc[k] = JSON.stringify(v);
      }
      return acc;
    }, {});
    options.processData = true;
  }

  options.data.session_key = sessionKey;
  var originalSuccess = options.success;
  options.success = function(){
    $spinner.hide();
    if(originalSuccess) originalSuccess.apply(this, arguments);
  };

  var originalError = options.error;
  options.error = function(){
    $spinner.hide();
    $('[role="page-error"]').show();
    if(originalError) originalError.apply(this, arguments);
    $.cookie('sessionKey', null);
    $.cookie('username', null);
  };

  return $.ajax.call($, options);
};

/* Generate puzzle */
var GeneratePuzzleView = Backbone.View.extend({

  tagName: 'div',
  template: _.template($('#generate-puzzle-template').html()),

  events: {
    'click [role="generate"]': 'generate',
    'click [role="cancel"]': 'hide',
  },

  initialize: function(){
    $('[role="generate-puzzle"]').empty().append(this.$el);
    this.$el.html(this.template());
    this.appendDictionaries();
    this.render();
  },

  appendDictionaries: function(){
    GeneratePuzzleView.dictionaries.each(function(o){
        var $option = $('<option>').attr('value', o.id).text(o.get('title'));
        this.$el.find('[role="select-dictionary"]').append($option);
    }, this);
  },

  generate: function(){
    Backbone.ajax({
        url: '/generate_puzzle',
        type: 'POST',
        data: {
            count: this.$el.find('[role="number"]').val(),
            dictionary: this.$el.find('[role="select-dictionary"]').val()
        }
    });
    this.hide();
  },

  render: function() {
    return this;
  },

  hide: function(){
    this.$el.parent().hide('fast', _.bind(this.remove, this));
  },

  show: function(){
    var coord = this.$el.parent().offset();
    coord.top = $(document).scrollTop();
    this.$el.parent().offset(coord);
    this.$el.parent().show('fast');
  }

});

/* Puzzle editor */

var Field = Backbone.Model.extend({

  initialize: function(){
    this.updateField();
    this.on('change:questions', this.updateField, this);
    this.on('change:current', this.updateField, this);
    this.set('errors', []);
  },

  updateField: function(){
    var result = {},
        errors = [],
        width = this.get('width'),
        height = this.get('height');

    // this function writes value into result and setting its type to error
    // if overlapping with other tokens
    var writeResultCheckConflict = function(coord, value){
      var x = parseInt(coord.split(':')[0]),
          y = parseInt(coord.split(':')[1]);
      if(!result[coord] || (result[coord].type === value.type && result[coord].value === value.value)){
        result[coord] = value;
        return true;
      } else {
        result[coord] = { type: 'error', hasHint : value.hasHint };
        return false;
      }
    };

    _(this.get('questions')).each(function(question){
      // Get coordinates for all answer letters.
      var coordinates = this._coordinatesFor(question.answer, question.answer_position);
      coordinates = _(coordinates).map(function(o){
        return [o[0] + question.column, o[1] + question.row, o[2]];
      });

      // Write hint:
      var coord = [question.column, question.row].join(':'),
          current = coord == this.get('current'),
          hasTokensOverBoard = !!_(coordinates).find(function(o){
                                 return (o[0] < 1 || o[0] > width || o[1] < 1 || o[1] > height);
                               }),
          hintValue = {
                        type : 'hint',
                        value: question.question_text,
                        hasHint: true,
                        arrow: question.answer_position,
                        current: current
                      },
          hasErrors = hasTokensOverBoard;

      var e = !writeResultCheckConflict(coord, hintValue);
      hasErrors = hasErrors || e;

      // Write tokens:
      _(coordinates).each(function(o){
         var value = {
           type : hasTokensOverBoard ? 'error' : 'value',
           value: o[2],
           current: current
         };

         var e = !writeResultCheckConflict(o.slice(0,2).join(':'), value);
         hasErrors = hasErrors || e;
      });
      if(hasErrors) errors.push(question);
    }, this);

    this.set('errors', errors);
    this.set('coordinates', result);
  },

   _coordinatesFor: function(answer, position_direction){
    var position = position_direction.split(':')[0],
        direction = position_direction.split(':')[1],
        simple_coords = _(answer).map(function(o, index){ return [index, 0, o]; });
    return this._adjustForPosition(position,
                                   this._adjustForDirection(direction,
                                                            simple_coords));
  },

  _adjustForDirection: function(direction, coords){
    switch(direction){
    case 'top':
      return _(coords).map(function(o){ return [o[1],-o[0], o[2]];});
    case 'left':
      return _(coords).map(function(o){ return [-o[0] , o[1], o[2]];});
    case 'right':
      return _(coords).map(function(o){ return [o[0] , o[1], o[2]];});
    case 'bottom':
      return _(coords).map(function(o){ return [o[1], o[0], o[2]];});
    default:
      throw 'not valid direction';
    }

  },

  _adjustForPosition: function(position, coords){
    switch(position){
    case 'north':
      return _(coords).map(function(o){ return [o[0], o[1] - 1, o[2]];});
    case 'north-east':
      return _(coords).map(function(o){ return [o[0] + 1, o[1] - 1, o[2]];});
    case 'east':
      return _(coords).map(function(o){ return [o[0] + 1, o[1], o[2]];});
    case 'south-east':
      return _(coords).map(function(o){ return [o[0] + 1, o[1] + 1, o[2]];});
    case 'south':
      return _(coords).map(function(o){ return [o[0], o[1] + 1, o[2]];});
    case 'south-west':
      return _(coords).map(function(o){ return [o[0] - 1, o[1] + 1, o[2]];});
    case 'west':
      return _(coords).map(function(o){ return [o[0] - 1, o[1], o[2]];});
    case 'north-west':
      return _(coords).map(function(o){ return [o[0] - 1, o[1] - 1, o[2]];});
    default:
      throw 'not valid position';
    }
  }
});

var Puzzle = Backbone.Model.extend({

  url: function(){
    if(this.id){
        return '/questions/' + this.id;
    } else {
        return '/questions';
    }
  },

  defaults: function(){
    return {
      "questions": [],
      "name": _.uniqueId('Сканворд'),
      "issuedAt": (new Date).toISOString().split('T')[0],
      "time_given": 60000,
      "height": 10,
      "width": 10
    };
  },

  initialize: function(){

    if(!this.get('author')) this.set('author', 'Аноним');
    if(!this.get('set_name')) this.set('set_name', this.get('set_id'));

    this.field = new Field({
      questions: this.get('questions'),
      width: this.get('width'),
      height: this.get('height')
    });

    this.on('change:height change:width', function(){
      this.field.set('width', this.get('width'));
      this.field.set('height', this.get('height'));
    }, this);

    this.on('change:questions', function(){
      this.field.set('questions', this.get('questions'));
    }, this);
  },

  addEmptyQuestion: function(x, y){
    if(typeof x == 'undefined' && typeof y == 'undefined'){
      x = 1;
      y = 1;
    }
    var question = {
      "column": x,
      "row": y,
      "question_text": "",
      "answer": "",
      "answer_position": "east:right"
    };
    this.set('questions', this.get('questions').concat([question]));
    this.trigger('questionAdded');
  }
});

var FieldView = Backbone.View.extend({

  tagName: 'div',
  id: "field",

  events: {
    'click .token': 'triggerClickEvent'
  },

  triggerClickEvent: function(e){
    var $token = $(e.target),
        type = $token.attr('data-type'),
        x = $token.attr('data-x'),
        y = $token.attr('data-y');

    this.trigger('tokenClick', { type: type, x: x, y: y });
  },

  initialize: function(){
    this.boxHeight = this.boxWidth = 50;
    this.render();
    this.model.on('change:height change:width', this.render, this);
    this.model.on('change:coordinates', this.updateCoordinates, this);
  },

  updateCoordinates: function(){
    _(this.coordinates).each(function(v, k){
        var x = k.split(':')[0],
            y = k.split(':')[1];
        this.setTokenFromCoordinates(v, x, y);
    }, this);
    return this;
  },

  render: function() {
    var height = this.model.get('height'),
        width = this.model.get('width'),
        x, y;
    this.coordinates = {};
    var $container = $('<div>').css({
                                      'height': (height + 1) * this.boxHeight + 'px',
                                      'width': (width + 1) * this.boxWidth + 'px',
                                      'position': 'relative'
                                    });
    // Add coordinates ruler
    for(x = 0; x <= width; x++){
      $container.append($('<div>').attr('data-x', x).attr('data-y', 0).attr('title', x).addClass('ruler'));
    }
    for(y = 1; y <= height; y++){
      $container.append($('<div>').attr('data-x', 0).attr('data-y', y).attr('title', y).addClass('ruler'));
    }

    // Tokens
    for(x = 1; x <= width; x++){
      for(y = 1; y <= height; y++){
        var $token = $('<div>').attr('data-x', x).attr('data-y', y).addClass('token');
        this.coordinates[[x, y].join(':')] = $token;
        this.setTokenFromCoordinates($token, x, y);
        $container.append($token);
      }
    }
    this.$el.html('').append($container);
    return this;
  },

  setTokenFromCoordinates: function($token, x, y){
    var properties = this.model.get('coordinates')[[x,y].join(':')];
    // set attributes
    if(properties){
      _(properties).each(function(v,k) { $token.attr('data-' + k, v); });
    } else {
      $token.attr('data-type', 'empty');
      $token.attr('data-current', 'false');
    }

    if(properties && (properties.type === 'hint' || properties.hasHint)){
      this.initializeDraggable($token);
    } else {
      this.removeDraggable($token);
    }
  },

  removeDraggable: function($token){
    if(!$token.data('draggable')) return null;
    $token.draggable('destroy');
    return null;
  },

  initializeDraggable: function($token){
    if($token.data('draggable')) return null;
    var self = this;
    $token.draggable({
      containment: 'parent',
      helper: 'clone',
      snap: '.token',
      addClasses: false,
      snapTolerance: 0,
      zIndex: 100,
      stop: function(event, ui){
        self.trigger('tokenMoved', {
                       source: {
                         x: parseInt(ui.helper.attr('data-x')),
                         y: parseInt(ui.helper.attr('data-y'))
                       },
                       destination: {
                         x: Math.round(ui.position.left / self.boxWidth),
                         y: Math.round(ui.position.top / self.boxHeight)
                       }
                     });
      }
    });
    return null;
  }
});


var PuzzleView = Backbone.View.extend({

  tagName: 'div',
  template: _.template($('#puzzle-editor-template').html()),
  rowTemplate: _.template($('#simple-puzzle-view-template').html()),

  events: {
    'click [role="print-set"]': 'printSet',
    'click [role="add_question"]': 'addQuestion',
    'click [role="delete_question"]': 'deleteQuestion',
    'keyup .question *': 'changeQuestion',
    'change .question *': 'changeQuestion',
    'click [role="save-puzzle"]': 'savePuzzle',
    'change [role^="puzzle"]': 'updatePuzzleAtribute',
    'click [role="cancel-puzzle"]': 'cancelPuzzle',
    'focus input, textarea': 'setCurrent',
    'blur input, textarea': 'removeCurrent',
    'click [role="current-arrow"]': 'showArrowPicker',
    'click [role="arrow-control-hover"] [data-arrow-control]': 'selectArrow'
  },

  showArrowPicker: function(e){
    e.preventDefault();
    $(e.target).parent().find('[role="arrow-control-hover"]').toggle();
  },

  selectArrow: function(e){
    e.preventDefault();
    var val = $(e.target).attr('data-arrow-control');
    $(e.target).closest('[role="arrow-control-hover"]').hide();
    $(e.target).closest('[role="question"]').find('[role="arrow-select"]').val(val).change();
    $(e.target).closest('[role="question"]').find('[role="current-arrow"]').attr('data-arrow-control', val);
  },

  setCurrent: function(e){
    var $question = $(e.target).closest('.question'),
        x = $question.find('.position.x').val(),
        y = $question.find('.position.y').val();
    this.model.field.set('current', [x, y].join(':'));
    $question.addClass('current');
  },

  removeCurrent: function(e){
    var $question = $(e.target).closest('.question');
    this.model.field.set('current', '');
    $question.removeClass('current');
  },

  addQuestion: function(){ this.model.addEmptyQuestion(); },

  deleteQuestion: function(e){
    $(e.target).closest('.question').remove();
    this.changeQuestion();
  },

  changeQuestion: function(){
    var self = this;
    var questions = this.$el.find('.question').map(function(){
      var $this = $(this);
      return {
        "column": parseInt($this.find('.position.x').val()),
        "row": parseInt($this.find('.position.y').val()),
        "question_text": $this.find('.hint').val(),
        "answer": $this.find('.answer').val().toLowerCase(),
        "answer_position": $this.find('select').val()
      };
    });
    this.model.set('questions', $.makeArray(questions));

    this.$el.find('.questions .question.current').map(function(){
      var $this = $(this),
          x = $this.find('.position.x').val(),
          y = $this.find('.position.y').val();
      self.model.field.set('current', [x, y].join(':'));
    });
  },

  findQuestion: function(x, y){
    return $('.question').
     filter(function(i, o){ return $(o).find('.position.x').val() == x;}).
     filter(function(i, o){ return $(o).find('.position.y').val() == y;}).
     first();
  },

  cancelPuzzle: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.trigger('cancel');
    this.hide();
  },

  rotateQuestion: function(x, y){
    var $q = this.findQuestion(x, y);
    var $s = $q.find('select');
    var val = $s.find(':selected + option').val();
    $s.val(val);
    $s.change();
    $q.find('[role="current-arrow"]').attr('data-arrow-control', val);
  },

  moveQuestion: function(e){
    var $question = this.findQuestion(e.source.x, e.source.y);
    $question.find('.position.x').val(e.destination.x);
    $question.find('.position.y').val(e.destination.y);
    $question.find('.position.x').keyup();
  },

  updatePuzzleAtribute: function(){
    var size = this.$el.find('[role="puzzle-size"]').val(),
        name = this.$el.find('[role="puzzle-name"]').val(),
        baseScore = this.$el.find('[role="puzzle-base-score"]').val(),
        timeGiven = this.$el.find('[role="puzzle-time-given"]').val(),
        setId = this.$el.find('[role="puzzle-set"]').val();

    this.model.set('name', name);
    this.model.set('set_id', setId);
    this.model.set('time_given', timeGiven);
    this.model.set('height', parseInt(size.split('x')[0]));
    this.model.set('width', parseInt(size.split('x')[1]));
  },

  initialize: function(){
    this.$el.html(this.template());
    this.puzzleSets = new PuzzleSets();
    puzzleSets.on('add', _.bind(this.render, this));
    var date = new Date();
    var nextMonth = new Date();
    if(nextMonth.getMonth() === 11){
        nextMonth.setYear(nextMonth.getFullYear() + 1);
        nextMonth.setMonth(0);
    } else {
        nextMonth.setMonth(nextMonth.Month() + 1);
    }
    puzzleSets.fetch({
        update: true,
        add: true,
        remove: false,
        data: {
            year: date.getFullYear(),
            month: (date.getMonth() + 1)
        }
    });
    puzzleSets.fetch({
        update: true,
        add: true,
        remove: false,
        data: {
            year: nextMonth.getFullYear(),
            month: (nextMonth.getMonth() + 1)
        }
    });

    $('[role="puzzle-editor"]').empty().append(this.$el);
    this.fieldView = new FieldView({ model: this.model.field, el: this.$el.find('[role="field"]')[0] });
    this.fieldView.on('tokenClick', function(e){
                   switch(e.type){
                   case 'empty':
                     this.model.addEmptyQuestion(e.x, e.y);
                     break;
                   case 'hint':
                     this.rotateQuestion(e.x, e.y);
                     break;
                   }
                 }, this);
    this.render();
    this.fieldView.on('tokenMoved', this.moveQuestion, this);
    this.model.on('questionAdded', this.render, this);
    this.model.field.on('change:errors', this.showErrors, this);
  },

  showErrors: function(){
    this.$el.find('.question').removeClass('error');
    _(this.model.field.get('errors')).each(function(error){
       this.findQuestion(error.column, error.row).addClass('error');
    }, this);
  },

  savePuzzle: function(){
    if(this.model.field.get('errors').length == 0){
        this.model.save();
        this.hide();
    }
  },

  render: function() {
    var $questions = this.$el.find('.questions'),
        $set = this.$el.find('[role="puzzle-set"]'),
        fieldSize = this.model.get('height') + 'x' + this.model.get('width');

    this.$el.find('[role="puzzle-size"]').val(fieldSize);
    this.$el.find('[role="puzzle-name"]').val(this.model.get('name'));
    this.$el.find('[role="puzzle-time-given"]').val(this.model.get('time_given'));

    $set.empty().append($('<option>'));
    puzzleSets.each(function(o){
        $option = $('<option>').val(o.get('id')).text(o.get('name'))
        $set.append($option);
    });
    $set.val(this.model.get('set_id'));

    $questions.empty();
    _(this.model.get('questions')).each(function(question){
      var $q = $(this.rowTemplate(question));
      $q.find('select').val(question.answer_position);
      $q.find('[role="current-arrow"]').attr('data-arrow-control', question.answer_position);
      $questions.append($q);
    }, this);

    return this;
  },

  hide: function(){
    this.$el.parent().hide('fast');
  },

  show: function(){
    var coord = this.$el.parent().offset();
    coord.top = $(document).scrollTop();
    this.$el.parent().offset(coord);
    this.$el.parent().show('fast');
  },

  printSet: function(e){
    if(e && e.preventDefault) e.preventDefault();
    html2canvas(this.$el.find('[role="field"] > div')[0],
                {
                  onrendered: function(canvas) {
                    window.open(canvas.toDataURL("image/png"));
                  }
                });
  }
});

var Puzzles = Backbone.Collection.extend({
  model: Puzzle,
  url: '/questions',
  comparator: false,
  parse: function(response) {
      this.currentPage = response.current_page;
      this.totalPages = response.total_pages;
      return response.puzzles;
  }
});

var PuzzlesView = Backbone.View.extend({
  tagName: 'div',
  rowTemplate: (function(){
      var t = '<tr>' +
              '<td><%= id %></td>' +
              '<td><%= name %></td>' +
              '<td><%= set_name %></td>' +
              '<td><%= author %></td>' +
              '<td><%= created_at %></td>' +
              '<td><button class="btn btn-small" role="edit-puzzle" data-id="<%= id %>">Редактировать</button></td>' +
              '</tr>';
      return _.template(t);
  })(),
  events: {
    'click [role="add-puzzle"]': 'addNewPuzzle',
    'click [role="new-generate-puzzle"]': 'generatePuzzle',
    'click [role="edit-puzzle"]': 'editPuzzle',
    'click [role="pagination"] a': 'selectPage',
    'change [role="filter"]': 'changeFilter'
  },

  initialize: function(options){
    var date = new Date(), nextMonth = new Date();
    if(nextMonth.getMonth() === 11){
        nextMonth.setYear(nextMonth.getFullYear() + 1);
        nextMonth.setMonth(0);
    } else {
        nextMonth.setMonth(nextMonth.Month() + 1);
    }
    this.dependentCollection = options.dependentCollection;
    this.puzzleSets = new PuzzleSets();
    this.collection.fetch();
    this.puzzleSets.fetch({
        update: true,
        add: true,
        remove: false,
        data: {
            year: date.getFullYear(),
            month: (date.getMonth() + 1)
        }
    });
    this.puzzleSets.fetch({
        update: true,
        add: true,
        remove: false,
        data: {
            year: nextMonth.getFullYear(),
            month: (nextMonth.getMonth() + 1)
        }
    });
    this.collection.on('sync', this.render, this);
    this.puzzleSets.on('sync', this.render, this);

  },

  render: function(){
    var rows = this.collection.map(function(o){
      return this.rowTemplate(o.toJSON());
    }, this).join('');

    this.$el.find('[role="rows"]').html(rows);
    this.renderPaginator();

    var $set = this.$el.find('[role="filter"]');
    $set.empty();
    $set.append($('<option>'));
    $set.append($('<option>').val('free').text('Непривязанные'));
    this.puzzleSets.each(function(o){
        $option = $('<option>').val(o.get('id')).text(o.get('name'))
        $set.append($option);
    });
    if(this.filter) $set.val(this.filter);

    return this;
  },

  changeFilter: function(e){
      e && e.preventDefault();
      var val = $(e.target).val(), data = {};
      if(val && val !== '') data.filter = val;
      this.filter = val;
      this.collection.fetch({reset: true, data: data });
  },

  addNewPuzzle: function(e){
      e && e.preventDefault();
      var puzzle = new Puzzle();
      puzzle.on('sync', _.bind(function(){ this.collection.fetch(); }, this));
      var puzzleView = new PuzzleView({ model: puzzle });
      puzzleView.show();
  },

  generatePuzzle: function(e){
      e && e.preventDefault();
      var generatePuzzleView = new GeneratePuzzleView();
      generatePuzzleView.show();
  },

  editPuzzle: function(e){
      var id = $(e.target).attr('data-id'),
          puzzle = this.collection.get(id);
      puzzle.on('sync', _.bind(function(){ this.collection.fetch(); this.dependentCollection.fetch(); }, this));
      var puzzleView = new PuzzleView({ model: puzzle });
      puzzleView.show();
  },

  renderPaginator: function(){
    var currentPage = this.collection.currentPage,
        totalPages = this.collection.totalPages,
        $result = $('<ul>');
    if(totalPages < 2) return;
    for(var i = 1; i <= totalPages; i++){
      var $li = $('<li>');
      $li.append($('<a>').attr('href', '#').text(i));
      if(i == currentPage) $li.addClass('active');
      $result.append($li);
    }
    this.$el.find('[role="pagination"]').empty().append($result);
  },

  selectPage: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var page = $(e.target).text();
    var data = { page: page };
    if(this.filter) data.filter = this.filter;
    this.collection.fetch({ reset: true, data: data });
  }
});

/* Signin Form */

var CurrentUser = Backbone.Model.extend({
  initialize: function(){
    if($.cookie('sessionKey')){
      this.set('signed', true);
      this.set('sessionKey',$.cookie('sessionKey'));
      this.set('username', $.cookie('username'));
    }
    this.on('change:signed', function(){
      if(this.get('signed')){
        $.cookie('sessionKey', this.get('sessionKey'), { expires: 7 });
        $.cookie('username', this.get('username'), { expires: 7 });
      } else {
        $.cookie('sessionKey', null);
        $.cookie('username', null);
      }
    }, this);
  },

  logout: function(){
    this.set('sessionKey', null);
    this.set('username', null);
    this.set('signed', false);
  },

  login: function(email, password){
    var self = this;
    $.ajax({
      type: "POST",
      url: '/login',
      cache: false,
      data: { email: email, password: password }
    }).done(function(data) {
      self.set('sessionKey', data.session_key);
      self.set('username', data.me.name);
      self.set('signed', true);
    }).error(function(error){
      if(error.status === 403){
        self.set('message', 'Неправилильный логин или пароль.');
      } else {
        self.set('message', 'Ошибка на сервере.');
      }
    });
  }
});

var LogoutView = Backbone.View.extend({
  tagName: "li",
  className: "pull-right logout",
  events: {
    'click [role="logout-button"]': 'logout'
  },

  initialize: function(){
    this.$el.attr('role', 'logout');
    this.model.on('change:username', function(){
      this.setUsername(this.model.get('username'));
    }, this);
    this.setUsername(this.model.get('username'));
  },

  logout: function(){
    this.model.logout();
  },

  setUsername: function(username){
    if(username){
      this.$el.find('[role="logout-username"]').text(username);
    } else {
      this.$el.find('[role="logout-username"]').text('');
    }
  }
});

var FormSigninView = Backbone.View.extend({
  tagName: 'div',
  className: "form-signin-container",

  events: {
    'submit': 'signin',
    'focus *': 'removeError'
  },

  initialize: function(){
    this.$el.attr('role', 'form-signin-container');
    this.model.on('change:signed', function(){
      if(this.model.get('signed')){
        this.hide();
      } else {
        this.show();
      }
    }, this);
    this.model.on('change:message', function(){
      var message = this.model.get('message');
      if(message){
        this.showError(message);
      } else {
        this.hideError();
      }

    }, this);

    if(this.model.get('signed')) this.hide();
  },

  signin: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var self = this;
    var $email = this.$el.find('[role="form-signin-email"]'),
        $password = this.$el.find('[role="form-signin-password"]');
    this.model.login($email.val(), $password.val());
    setTimeout(function(){
      $email.val('');
      $password.val('');
    }, 1000);
  },

  removeError: function(){
    this.model.set('message', null);
  },

  showError: function(message){
    this.$el.find('[role="form-signin-error"]').text(message).show('fast');
  },

  hideError: function(message){
    this.$el.find('[role="form-signin-error"]').hide('fast');
  },

  hide: function(){
    this.$el.hide('fast');
  },

  show: function(){
    this.$el.show('fast');
  }
});


/* Puzzle sets */

var PuzzleSet = Backbone.Model.extend({
  urlRoot: '/sets',
  defaults: function(){
    return {
      published: false,
      puzzle_ids: [],
      type: '',
      month: ((new Date).getMonth() + 1),
      year: (new Date).getFullYear(),
      name: _.uniqueId('Сет ')
    };
  }
});

var PuzzleSets = Backbone.Collection.extend({
  model: PuzzleSet,
  url: '/sets',
  comparator: false,
  parse: function(response) {
    return response.sets;
  }
});

var PuzzleSetView = Backbone.View.extend({
  className: 'row m-bottom-20px',
  showTemplate: _.template($('#set-view-template').html()),
  editTemplate: _.template($('#set-view-editor-template').html()),
  editPuzzleTemplate: _.template(
      '<input type="text" role="puzzle-id">' +
      '<input class="btn" type="button" role="delete-puzzle" value="delete">'
  ),
  events: {
    'click [role="edit-set"]': 'startEditing',
    'click [role="add-puzzle"]': 'addPuzzle',
    'change [role="set-name"]': 'changeName',
    'change [role="set-type"]': 'changeType',
    'click [role="delete-puzzle"]': 'deletePuzzle',
    'change [role="puzzle-id"]': 'updatePuzzles',
    'click [role="delete-set"]': 'deletePuzzleSet',
    'click [role="save-set"]': 'saveSet',
    'click [role="publish-set"]': 'publishSet',
    'click [role="cancel-set"]': 'cancelSet'
  },

  initialize: function(){
    this.editing = false;
    this.model.on('change:puzzles', this.render, this);
  },

  render: function(){
    var template;
    if(this.editing){
      template = this.editTemplate;
    } else {
      template = this.showTemplate;
    }
    this.$el.empty().append(template( this.model.toJSON() ));
    this.$el.find('[role="set-type"]').val(this.model.get('type'));
    return this;
  },

  startEditing: function(){
    this.editing = true;
    this.render();
  },

  stopEditing: function(){
    this.editing = false;
  },

  addPuzzle: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var $div = $('<div>').html(this.editPuzzleTemplate());
    this.$el.find('[role="puzzle-ids"]').append($div);
  },

  changeName: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.set('name', $(e.target).val());
    return true;
  },

  changeType: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.set('type', $(e.target).val());
    return true;
  },

  deletePuzzle: function(e){
    if(e && e.preventDefault) e.preventDefault();
    $(e.target).parent().remove();
    updatePuzzles();
  },

  updatePuzzles: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var puzzleIds = this.$el.find('[role="puzzle-id"]')
          .map(function(){ return $(this).val(); }).toArray();
    puzzleIds = _(puzzleIds).reject(function(o){
        return o === "";
    });
    this.model.set('puzzle_ids', puzzleIds);
  },

  deletePuzzleSet: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.destroy();
    this.remove();
  },

  cancelSet: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.stopEditing();
    this.render();
  },

  saveSet: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.save();
    this.stopEditing();
    this.render();
  },

  publishSet: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.set('published', true);
    this.model.save();
    this.stopEditing();
    this.render();
  }
});

var PuzzleSetsView = Backbone.View.extend({
  tagName: 'div',
  emptySetsTemplate: _.template($('#set-view-empty-template').html()),
  events: {
    'changeMonth': 'changeMonth',
    'click [role="add-set"]': 'addNewSet'
  },

  initialize: function(options){
    this.$el.attr('role', 'sets');
    this.collection.on('reset', this.render, this);
    this.dependentCollection = options.dependentCollection;

    $('[role="sets-datepicker"]').datepicker({
      format: 'MM yyyy',
      startView: 1,
      language: 'ru'
    });
    this.changeMonth({ 'date': new Date() });
  },

  render: function(){
    var $setsContainer = this.$el.find('[role="sets-container"]');
    if(this.collection.size() === 0){
      $setsContainer.empty().append(this.emptySetsTemplate());
    } else {
      $setsContainer.empty();
      this.collection.each(this.addSet, this);
    }
    return this;
  },

  addNewSet: function(){
    var set = new PuzzleSet(),
        date = this.$el.find('[role="sets-datepicker"]').data('datepicker').date;
    set.set('month', date.getMonth() + 1);
    set.set('year', date.getFullYear());
    this.addSet(set, true);
  },

  addSet: function(set, editing){
    this.$el.find('[role="empty-set"]').remove();
    var view = new PuzzleSetView({ model: set });
    set.on('sync', function(){ this.dependentCollection.fetch() }, this);
    if(editing === true) view.editing = true;
    this.$el.find('[role="sets-container"]').append(view.render().el);
  },

  changeMonth: function(e){
    var dp = this.$el.find('[role="sets-datepicker"]');
    dp.data('datepicker').date = e.date;
    dp.data('datepicker').setValue();
    dp.data('datepicker').hide();
    this.$el.find('[role="current-date-description"]').text(dp.data('date'));
    this.collection.fetch({ data: { year: e.date.getFullYear(),
                                    month: (e.date.getMonth() + 1) } });
  }
});


/* Users tab */

var User = Backbone.Model.extend({
    initialize: function(){

    },

    updateRole: function(role){
      this.set('role', role);
      Backbone.ajax({
          url: '/users/' + this.id + '/change_role',
          type: 'POST',
          data: { role: role }
      });
  }
});

var Users = Backbone.Collection.extend({
  model: User,
  url: '/users/top',
  comparator: false,
  parse: function(response) {
    this.currentPage = response.current_page;
    this.totalPages = response.total_pages;
    return response.users;
  }
});

var UsersView = Backbone.View.extend({
  tagName: 'div',
  rowTemplate: _.template('<tr><td><%= name %></td><td><%= surname %></td><td><a href="mailto:<%= email %>"><%= email %></a></td><td><%= providersLink(providers) %></td>' +
                          '<td><%= typeof(userpic) !== "undefined" ? userpicLink(userpic) : "нет фото" %></td>' +
                          '<td><%= solved %></td><td><%= month_score %></td><td><%= showIsCheater(is_cheater)  %></td><td><button class="btn" role="scores" data-id="<%= id %>">начисления</button></td></tr>'),
  events: {
    'click [role="pagination"] a': 'selectPage',
    'click [role="scores"]': 'showScore',
    'change [role="user-role"]': 'changeUserRole'
  },

  helpers: {
      userpicLink: function(userpic){
          if(userpic.match(/graph.facebook/) userpic = userpic.replace(/((width|height)=[0-9]*&?)+/, 'width=1000&height=1000');
          return '<a href="'+ userpic + '" download="image">аватар</a>'
      },

      providersLink: function(providers){
          var result = '',
              fb = _(providers).find(function(o){ return o.provider_name == 'facebook'; });
              vk = _(providers).find(function(o){ return o.provider_name == 'vkontakte'; });

          if(fb) result += '<a href="http://facebook.com/'+ fb.provider_id + '">fb</a>';
          result += ' ';
          if(vk) result += '<a href="http://vk.com/id'+ vk.provider_id + '">vk</a>';
          return result;
      },

      showIsCheater: function(is_cheater){
          return is_cheater ? is_cheater : "не читер";
      }
  },

  initialize: function(){
    this.$el.attr('role', 'users');
    this.collection.on('reset', this.render, this);
    this.collection.fetch();
  },

  render: function(){
    var $el = this.$el;
    // Rendering row templates to view
    var rows = this.collection.map(function(o){
      return this.rowTemplate(_.extend({}, this.helpers, o.toJSON()));
    }, this).join('');
    $el.find('[role="rows"]').html(rows);
    // Setting correct role for users
    this.collection.map(function(o){
        var $select = $el.find('[role="user-role"][data-user-id="'+
                               o.id
                               +'"]');
        $select.val(o.get('role'));
    });
    this.renderPaginator();
    return this;
  },

  renderPaginator: function(){
    var currentPage = this.collection.currentPage,
        totalPages = this.collection.totalPages,
        $result = $('<ul>');
    if(totalPages < 2) return;
    for(var i = 1; i <= totalPages; i++){
      var $li = $('<li>');
      $li.append($('<a>').attr('href', '#').text(i));
      if(i == currentPage) $li.addClass('active');
      $result.append($li);
    }
    this.$el.find('[role="pagination"]').empty().append($result);
  },

  selectPage: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var page = $(e.target).text();
    this.collection.fetch({ data: { page: page }});
  },

  showScore: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var user = this.collection.get($(e.target).attr('data-id'));
    var scores = _(user.get('scores')).map(function(o){ o = JSON.parse(o); return [o.source, o.score].join(':') }).join("\n");
    $('[role="modal-body"]').text(scores);
    $('[role="scores-modal"]').modal();
  },

  changeUserRole: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var $el = $(e.target),
        id = $el.attr('data-user-id'),
        user = this.collection.get(id),
        value = $el.val();
      user.updateRole(value);
  }
});

/* Dictionaries */

var DictionaryEditorView = Backbone.View.extend({

  tagName: 'div',
  template: _.template($('#dictionary-editor-template').html()),

  events: {
      'click [role="save"]': 'saveDictionary',
      'click [role="cancel"]': 'hide',
      'click [role="delete"]': 'delete',
      'change [role="dict"]': 'uploadDict',
      'change [role="title"]': 'changeTitle'
  },

  initialize: function(){
    $('[role="dictionary-editor"]').empty().append(this.$el);
    var data = _.extend({ isNew: this.model.isNew()}, this.model.toJSON());
    this.$el.html(this.template(data));
    this.render();
  },

  changeTitle: function(){
    this.model.set('title', this.$el.find('[role="title"]').val());
  },

  uploadDict: function(){
    $('[role="loading-spinner"]').show();
    var file =  this.$el.find('[role="dict"]')[0].files[0];
    var fr = new FileReader();
    fr.onload = _.bind(this.uploaded, this);
    fr.readAsText(file);
  },

  uploaded: function(e){
    this.model.set('body', e.target.result);
    $('[role="loading-spinner"]').hide();
  },

  saveDictionary: function(){
    this.model.save();
    this.hide();
  },


  delete: function(){
    this.model.destroy();
    this.hide();
  },

  render: function() {
    return this;
  },

  hide: function(){
    this.$el.parent().hide('fast', _.bind(this.remove, this));
  },

  show: function(){
    var coord = this.$el.parent().offset();
    coord.top = $(document).scrollTop();
    this.$el.parent().offset(coord);
    this.$el.parent().show('fast');
  }

});

var Dictionary = Backbone.Model.extend({
    initialize: function(){
     if(!this.get('title')) this.set('title', '');
    },
    url: function(){
      return this.isNew() ? '/dictionaries' : '/dictionaries/' + this.id;
    }
});

var Dictionaries = Backbone.Collection.extend({
  model: Dictionary,
  url: '/dictionaries',
  comparator: false,
  parse: function(response) {
    this.currentPage = response.current_page;
    this.totalPages = response.total_pages;
    return response.dictionaries;
  }
});

var DictionariesView = Backbone.View.extend({
  tagName: 'div',
  rowTemplate: _.template('<tr><td><%= title %></td><td><%= words_count %></td><td>' +
                          '<button class="btn btn-small" role="edit-dictionary" data-dictionary-id="<%= id %>">Редактировать</button></td></tr>'),
  events: {
    'click [role="pagination"] a': 'selectPage',
    'click [role="edit-dictionary"]': 'editDictionary',
    'click [role="add-dictionary"]':  'addDictionary'
  },

  initialize: function(){
    this.collection.on('reset', this.render, this);
    this.collection.fetch();
  },

  render: function(){
    var $el = this.$el;
    // Rendering row templates to view
    var rows = this.collection.map(function(o){
      return this.rowTemplate(o.toJSON());
    }, this).join('');
    $el.find('[role="rows"]').html(rows);
    this.renderPaginator();
    return this;
  },

  renderPaginator: function(){
    var currentPage = this.collection.currentPage,
        totalPages = this.collection.totalPages,
        $result = $('<ul>');
    if(totalPages < 2) return;
    for(var i = 1; i <= totalPages; i++){
      var $li = $('<li>');
      $li.append($('<a>').attr('href', '#').text(i));
      if(i == currentPage) $li.addClass('active');
      $result.append($li);
    }
    this.$el.find('[role="pagination"]').empty().append($result);
  },

  selectPage: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var page = $(e.target).text();
    this.collection.fetch({ data: { page: page }});
  },

  addDictionary: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var dictionary = new Dictionary();
    dictionary.on('sync', _.bind(function(){ this.collection.fetch(); }, this));
    var dictionaryView = new DictionaryEditorView({ model: dictionary });
    dictionaryView.show();
  },

  editDictionary: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var $el = $(e.target),
        id = $el.attr('data-dictionary-id'),
        dictionary = this.collection.get(id);
    dictionary.on('sync', _.bind(function(){ this.collection.fetch(); }, this));
    var dictionaryView = new DictionaryEditorView({ model: dictionary });
    dictionaryView.show();
  }
});


/* Dashboard */

var Counters = Backbone.Model.extend({
  url: '/counters',
  toData: function(){
    var days = ['День'].concat(this.get('days')),
        logins = ['логины'].concat(this.get('logins')),
        sets_bought = ['покупки сетов'].concat(this.get('sets_bought')),
        hints_bought = ['покупки подсказок'].concat(this.get('hints_bought')),
        scored = ['увеличение счета'].concat(this.get('scored'));
    return _.zip(days, logins, sets_bought, hints_bought, scored);
  }
});


var DashboardView = Backbone.View.extend({

  tagName: 'div',
  className: 'row',

  events: {
  },

  initialize: function(){
    this.counters = new Counters();
    this.counters.on('change', this.renderChart, this);
    this.counters.fetch();
    this.render();
  },

  renderChart: function(){
    var data = google.visualization.arrayToDataTable(this.counters.toData());
    var options = {
      chartArea: { left: 40, width: 900 },
      legend: { position: 'top' }
    };

    var chart = new google.visualization.LineChart(this.$el.find('[role="chart"]')[0]);
    chart.draw(data, options);
  },

  render: function() {}
});



/* Service message */

var ServiceMessage = Backbone.Model.extend({
  url: '/service_messages',
  isNew: function(){ return false; }
});

var ServiceMessageView = Backbone.View.extend({
  tagName: 'div',

  events: {
    'change [role^="service-message-text"]' : 'updateServiceMessage',
    'click [role="save-service-message"]': 'saveServiceMessage'
  },

  initialize: function(){
    this.model.on('change', this.render, this);
    this.model.fetch();
  },

  render: function(){
    this.$el.find('[role="service-message-text1"]').val(this.model.get('message1') || "");
    this.$el.find('[role="service-message-text2"]').val(this.model.get('message2') || "");
    this.$el.find('[role="service-message-text3"]').val(this.model.get('message3') || "");
  },

  updateServiceMessage: function(e){
    this.model.set('message1', this.$el.find('[role="service-message-text1"]').val());
    this.model.set('message2', this.$el.find('[role="service-message-text2"]').val());
    this.model.set('message3', this.$el.find('[role="service-message-text3"]').val());
  },

  saveServiceMessage: function(){
    this.model.save();
  }
});


/* Coefficients */

var Coefficients = Backbone.Model.extend({
  url: '/coefficients',
  isNew: function(){ return false; }
});

var CoefficientsView = Backbone.View.extend({
  tagName: 'div',

  events: {
    'change [role="time-bonus"]'           : 'updateCoefficients',
    'change [role="friend-bonus"]'         : 'updateCoefficients',
    'change [role="free-base-score"]'      : 'updateCoefficients',
    'change [role="gold-base-score"]'      : 'updateCoefficients',
    'change [role="brilliant-base-score"]' : 'updateCoefficients',
    'change [role="silver1-base-score"]'   : 'updateCoefficients',
    'change [role="silver2-base-score"]'   : 'updateCoefficients',
    'click [role="save-coefficients"]'     : 'saveCoefficients'
  },

  initialize: function(){
    this.model.on('change', this.render, this);
    this.model.fetch();
  },

  render: function(){
    this.$el.find('[role="time-bonus"]').val(this.model.get("time-bonus") || "");
    this.$el.find('[role="friend-bonus"]').val(this.model.get("friend-bonus") || "");
    this.$el.find('[role="free-base-score"]').val(this.model.get("free-base-score") || "");
    this.$el.find('[role="gold-base-score"]').val(this.model.get("gold-base-score") || "");
    this.$el.find('[role="brilliant-base-score"]').val(this.model.get("brilliant-base-score") || "");
    this.$el.find('[role="silver1-base-score"]').val(this.model.get("silver1-base-score") || "");
    this.$el.find('[role="silver2-base-score"]').val(this.model.get("silver2-base-score") || "");
  },

  updateCoefficients: function(e){
    this.model.set("time-bonus", this.$el.find('[role="time-bonus"]').val());
    this.model.set("friend-bonus", this.$el.find('[role="friend-bonus"]').val());
    this.model.set("free-base-score", this.$el.find('[role="free-base-score"]').val());
    this.model.set("gold-base-score", this.$el.find('[role="gold-base-score"]').val());
    this.model.set("brilliant-base-score", this.$el.find('[role="brilliant-base-score"]').val());
    this.model.set("silver1-base-score", this.$el.find('[role="silver1-base-score"]').val());
    this.model.set("silver2-base-score", this.$el.find('[role="silver2-base-score"]').val());
  },

  saveCoefficients: function(){
    this.model.save();
  }
});


/* Notifications */

var NotificationsView = Backbone.View.extend({
  tagName: 'div',

  events: {
    'click [role="send-notification"]': 'sendNotification'
  },

  initialize: function(){
  },

  render: function(){
  },

  sendNotification: function(){
    var $notificationText = this.$el.find('[role="notification-text"]'),
        message = $notificationText.val();

    $notificationText.val('');
    Backbone.ajax({ data: { message: message }, type: 'POST', 'url': '/push_message' });
  }
});


/* Initializnig code */

var puzzleView, formSigninView, logoutView, puzzleSets, puzzles, puzzlesView, puzzleSetsView, users, usersView, dashboardView, serviceMessage, serviceMessageView, coefficients, coefficientsView, notificationsView, dictionaries, dictionariesView;

$(function(){
  currentUser = new CurrentUser();
  formSigninView = new FormSigninView({ model: currentUser,
                                        el: $('[role="form-signin-container"]')[0] });
  logoutView = new LogoutView({ model: currentUser,
                                el: $('[role="logout"]')[0] });

  users = new Users;
  usersView = new UsersView({ el: $('[role="users"]')[0], collection: users });

  puzzles = new Puzzles();
  puzzlesView = new PuzzlesView({ el: $('[role="puzzles"]')[0], collection: puzzles, dependentCollection: puzzleSets });

  puzzleSets = new PuzzleSets();
  puzzleSetsView = new PuzzleSetsView({ el: $('[role="sets"]')[0], collection: puzzleSets, dependentCollection: puzzles });

  dashboardView = new DashboardView({ el: $('[role="dashboard"]')[0] });

  serviceMessageView = new ServiceMessageView({
                                                el: $('[role="service-message"]')[0],
                                                model: new ServiceMessage()
                                              });

  coefficientsView = new CoefficientsView({
                                            el: $('[role="coefficients"]')[0],
                                            model: new Coefficients()
                                          });

  notificationsView = new NotificationsView({
                                            el: $('[role="notifications"]')[0]
                                          });

  dictionaries = new Dictionaries();
  GeneratePuzzleView.dictionaries = dictionaries;

  dictionariesView = new DictionariesView({ el: $('[role="dictionaries"]')[0], collection: dictionaries });

  $('[role="loading-spinner"]').hide();
});
