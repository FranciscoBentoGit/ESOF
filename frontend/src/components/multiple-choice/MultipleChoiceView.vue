<template>
  <ul>
    <li v-for="option in questionDetails.options" :key="option.id">
      <span
        v-if="option.correct && !questionDetails.isToOrder"
        v-html="
          convertMarkDown(
            studentAnswered(option.id) + '**[★]** ' + option.content
          )
        "
        v-bind:class="[option.correct ? 'font-weight-bold' : '']"
      />
      <span
        v-else-if="option.correct && questionDetails.isToOrder"
        v-html="
          convertMarkDown(
            studentAnswered(option.id) + '**[★]** ' + option.content + ' -- order: ' + option.order
          )
        "
        v-bind:class="[option.correct ? 'font-weight-bold' : '']"
      />
      <span
        v-else
        v-html="convertMarkDown(studentAnswered(option.id) + option.content)"
      />
    </li>
  </ul>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import MultipleChoiceQuestionDetails from '@/models/management/questions/MultipleChoiceQuestionDetails';
import MultipleChoiceAnswerDetails from '@/models/management/questions/MultipleChoiceAnswerDetails';

@Component
export default class MultipleChoiceView extends Vue {
  @Prop() readonly questionDetails!: MultipleChoiceQuestionDetails;
  @Prop() readonly answerDetails?: MultipleChoiceAnswerDetails;

  studentAnswered(option: number) {
    return this.answerDetails && this.answerDetails.optionsList.filter(any => any.id == option).length == 1
      ? '**[S]** '
      : '';
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
