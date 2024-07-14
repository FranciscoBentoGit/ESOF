<template>
  <ul>
    <span
      v-html="convertMarkDown(questionDetails.response)"
      v-bind:class="[questionDetails.response ? 'font-weight-bold' : '']"
    />
  </ul>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';
import OpenAnswerAnswerDetails from '@/models/management/questions/OpenAnswerAnswerDetails';

@Component
export default class ShowOpenAnswerQuestion extends Vue {
  @Prop() readonly questionDetails!: OpenAnswerQuestionDetails;
  @Prop() readonly answerDetails?: OpenAnswerAnswerDetails;
  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
