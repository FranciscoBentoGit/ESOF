<!--
Used on:
  - QuestionComponent.vue
  - ResultComponent.vue
-->

<template>
  <ul data-cy="optionList" class="option-list">
    <li
      v-for="(n, index) in questionDetails.options.length"
      :key="index"
      v-bind:class="['option', optionClass(index, questionDetails.isToOrder)]"
      @click="
        !isReadonly && selectOption(questionDetails.options[index].optionId, questionDetails.options[index].order)
      "
    >
      <span
        v-if="
          isReadonly &&
            isCorrect(questionDetails.options[index].optionId, questionDetails.options[index].order)
        "
        class="fas fa-check option-letter"
      />
      <span
        v-else-if="
          isReadonly &&
            isWrong(questionDetails.options[index].optionId, questionDetails.options[index].order)
        "
        class="fas fa-times option-letter"
      />
      <span v-else class="option-letter">{{
        String.fromCharCode(65 + index)
      }}</span>
      <span
        class="option-content"
        v-html="convertMarkDown(questionDetails.options[index].content)"
      />
      <p v-if="questionDetails.isToOrder">
        <v-select
          :items="availableOptions"
          v-model="answerDetails.optionsIdsHash[questionDetails.options[index].optionId]"
        />
      <p/>         
    </li>
  </ul>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model, Emit } from 'vue-property-decorator';
import MultipleChoiceStatementQuestionDetails from '@/models/statement/questions/MultipleChoiceStatementQuestionDetails';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import MultipleChoiceStatementAnswerDetails from '@/models/statement/questions/MultipleChoiceStatementAnswerDetails';
import MultipleChoiceStatementCorrectAnswerDetails from '@/models/statement/questions/MultipleChoiceStatementCorrectAnswerDetails';

@Component
export default class MultipleChoiceAnswer extends Vue {
  @Prop(MultipleChoiceStatementQuestionDetails)
  readonly questionDetails!: MultipleChoiceStatementQuestionDetails;//options
  @Prop(MultipleChoiceStatementAnswerDetails)
  answerDetails!: MultipleChoiceStatementAnswerDetails;//optionsIdsHash
  @Prop(MultipleChoiceStatementCorrectAnswerDetails)
  readonly correctAnswerDetails?: MultipleChoiceStatementCorrectAnswerDetails;//correctOptions

  get isReadonly() {
    return !!this.correctAnswerDetails;
  }

  get availableOptions(): number[] {
    var array = new Array()
    var j = 0;
    for (var i = 0; i < this.questionDetails.options.length; i++) {
      j++;
      array.push(j);
    }  
    return array;
  }

  isCorrect(optionId: number, order: number) {
    if (!this.correctAnswerDetails) {
      return false;
    }
    else {
      for (let key in this.correctAnswerDetails.correctOptions) {
        console.log(parseInt(key))
        console.log(optionId)
        if (parseInt(key) == optionId) {
          return true;
        }
      }
      return false;
    }
  }

  isWrong(optionId: number, order: number) {
    if (this.answerDetails.optionsIdsHash.length != 0) {
      for (let [key, value] of Object.entries(this.answerDetails.optionsIdsHash)) {
        if (parseInt(key) == optionId && value == order) {
          return true;
        }
      }
      return false;
    }
    return false;
  }

  optionClass(index: number, isToOrder: boolean) {
    if (this.isReadonly) {
      if (!!this.correctAnswerDetails) {
        for (let [key, value] of Object.entries(this.correctAnswerDetails.correctOptions)) {
          if (parseInt(key) === this.questionDetails.options[index].optionId && value === this.questionDetails.options[index].order)
            return 'correct';
        }    
      } else if (this.answerDetails.optionsIdsHash.length != 0) {
          for (let [key, value] of Object.entries(this.answerDetails.optionsIdsHash)) {
            if (parseInt(key) === this.questionDetails.options[index].optionId && value === this.questionDetails.options[index].order)
              return 'wrong';
            }
      } else {
        return 'wrong';
      }
    } else {
      let selected = false;
      let id = this.questionDetails.options[index].optionId;
      for (let [key, value] of Object.entries(this.answerDetails.optionsIdsHash)) {
        if (parseInt(key) == id && value === this.questionDetails.options[index].order)
          selected = true;
      }
        return selected
          ? 'selected'
          : '';
    }
  }

 
  @Emit('question-answer-update')
  selectOption(optionId: number, order: number) {
    let exists = false;
    for(const [key, value] of Object.entries(this.answerDetails.optionsIdsHash)) {
      if (parseInt(key) == optionId && value == order) {
        exists = true;
      }
    }
    
    if (exists) {
      delete this.answerDetails.optionsIdsHash[optionId];
      this.answerDetails.optionsIdsHash = Object.assign({}, this.answerDetails.optionsIdsHash)
    } else {
      this.answerDetails.optionsIdsHash[optionId] = order;
      this.answerDetails.optionsIdsHash = Object.assign({}, this.answerDetails.optionsIdsHash)
    }
  }
 

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>

<style lang="scss" scoped>
.unanswered {
  .correct {
    .option-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #333333 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}

.correct-question {
  .correct {
    .option-content {
      background-color: #299455;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #299455 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}

.incorrect-question {
  .wrong {
    .option-content {
      background-color: #cf2323;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #cf2323 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
  .correct {
    .option-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #333333 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}
</style>
